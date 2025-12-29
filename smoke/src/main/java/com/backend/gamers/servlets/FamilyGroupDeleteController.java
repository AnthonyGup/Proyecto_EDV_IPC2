package com.backend.gamers.servlets;

import com.backend.daos.FamilyGroupDao;
import com.backend.daos.GroupMemberDao;
import com.backend.daos.LibraryDao;
import com.backend.db.DBConnection;
import com.backend.entities.FamilyGroup;
import com.backend.entities.GroupMember;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "FamilyGroupDeleteController", urlPatterns = {"/family-group/delete"})
public class FamilyGroupDeleteController extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String groupIdStr = request.getParameter("groupId");
        String requesterId = request.getParameter("userId");

        if (groupIdStr == null || groupIdStr.isBlank() || requesterId == null || requesterId.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"message\":\"groupId y userId son requeridos\"}");
            return;
        }

        int groupId;
        try {
            groupId = Integer.parseInt(groupIdStr);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"message\":\"groupId inválido\"}");
            return;
        }

        FamilyGroupDao fgDao = new FamilyGroupDao("familyGroup", "group_id");
        GroupMemberDao gmDao = new GroupMemberDao("groupMember", "");

        try {
            // Verificar que el grupo existe
            FamilyGroup group = fgDao.readByPk(String.valueOf(groupId));
            if (group == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"message\":\"Grupo no encontrado\"}");
                return;
            }

            // Verificar que el solicitante es el dueño del grupo
            if (!group.getOwnerId().equals(requesterId)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"message\":\"Solo el dueño puede eliminar el grupo\"}");
                return;
            }

            // Obtener todos los miembros antes de eliminar el grupo
            List<GroupMember> members = gmDao.listMembersByGroup(groupId);

            // Limpiar library: eliminar juegos no comprados de los miembros
            cleanupLibrariesForMembers(members, gmDao);

            // Eliminar el grupo (las FK CASCADE eliminarán groupMember e invitation automáticamente)
            boolean deleted = fgDao.delete(String.valueOf(groupId));
            if (!deleted) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"message\":\"No se pudo eliminar el grupo\"}");
                return;
            }

            JsonObject success = new JsonObject();
            success.addProperty("message", "Grupo eliminado exitosamente");
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            try (PrintWriter out = response.getWriter()) {
                out.print(gson.toJson(success));
                out.flush();
            }

        } catch (SQLException ex) {
            Logger.getLogger(FamilyGroupDeleteController.class.getName()).log(Level.SEVERE, null, ex);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"message\":\"Error al eliminar el grupo\"}");
        }
    }

    /**
     * Limpia las librerías de los miembros del grupo:
     * - Para cada miembro, obtiene los grupos a los que aún pertenece (excluyendo el que se está eliminando).
     * - Recopila todos los juegos comprados de esos otros grupos.
     * - Elimina de la library del miembro todos los juegos con buyed=FALSE que no estén en otro grupo.
     */
    private void cleanupLibrariesForMembers(List<GroupMember> members, GroupMemberDao gmDao) throws SQLException {
        LibraryDao libDao = new LibraryDao("`library`", "library_id");
        Connection conn = DBConnection.getInstance().getConnection();

        for (GroupMember member : members) {
            String userId = member.getUserId();

            // Obtener otros grupos del usuario
            List<com.backend.entities.FamilyGroup> otherGroups = gmDao.listGroupsByUser(userId);
            // Filtrar el grupo actual (que se está eliminando)
            otherGroups.removeIf(g -> g.getGroupId() == member.getFamilyGroupId());

            // Si el usuario sigue en otros grupos, recopilar juegos comprados de esos grupos
            List<Integer> protectedGameIds = new java.util.ArrayList<>();
            if (!otherGroups.isEmpty()) {
                for (com.backend.entities.FamilyGroup otherGroup : otherGroups) {
                    List<GroupMember> otherMembers = gmDao.listMembersByGroup(otherGroup.getGroupId());
                    for (GroupMember om : otherMembers) {
                        String memberSql = "SELECT DISTINCT game_id FROM `library` WHERE user_id = ? AND buyed = TRUE";
                        PreparedStatement pstmt = conn.prepareStatement(memberSql);
                        pstmt.setString(1, om.getUserId());
                        java.sql.ResultSet rs = pstmt.executeQuery();
                        while (rs.next()) {
                            int gid = rs.getInt("game_id");
                            if (!protectedGameIds.contains(gid)) {
                                protectedGameIds.add(gid);
                            }
                        }
                    }
                }
            }

            // Eliminar juegos no comprados (buyed=FALSE) del usuario, excepto los protegidos
            String deleteSql;
            PreparedStatement dstmt;
            if (protectedGameIds.isEmpty()) {
                // Sin otros grupos, eliminar todos los no comprados
                deleteSql = "DELETE FROM `library` WHERE user_id = ? AND buyed = FALSE";
                dstmt = conn.prepareStatement(deleteSql);
                dstmt.setString(1, userId);
            } else {
                // Conservar solo los protegidos
                StringBuilder placeholders = new StringBuilder();
                for (int i = 0; i < protectedGameIds.size(); i++) {
                    if (i > 0) placeholders.append(",");
                    placeholders.append("?");
                }
                deleteSql = "DELETE FROM `library` WHERE user_id = ? AND buyed = FALSE AND game_id NOT IN (" + placeholders + ")";
                dstmt = conn.prepareStatement(deleteSql);
                dstmt.setString(1, userId);
                for (int i = 0; i < protectedGameIds.size(); i++) {
                    dstmt.setInt(i + 2, protectedGameIds.get(i));
                }
            }
            dstmt.executeUpdate();
        }
    }
}
