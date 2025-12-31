package com.backend.gamers.servlets;

import com.backend.daos.FamilyGroupDao;
import com.backend.daos.GroupMemberDao;
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

@WebServlet(name = "FamilyGroupMemberDeleteController", urlPatterns = {"/family-group/member"})
public class FamilyGroupMemberDeleteController extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String groupIdStr = request.getParameter("groupId");
        String memberEmail = request.getParameter("memberEmail");
        String ownerEmail = request.getParameter("ownerEmail");

        if (groupIdStr == null || memberEmail == null || ownerEmail == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"groupId, memberEmail y ownerEmail son requeridos\"}");
            return;
        }

        int groupId;
        try {
            groupId = Integer.parseInt(groupIdStr);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"groupId inválido\"}");
            return;
        }

        FamilyGroupDao fgDao = new FamilyGroupDao("familyGroup", "group_id");
        GroupMemberDao gmDao = new GroupMemberDao("groupMember", "");

        try {
            // Verificar que el grupo existe
            FamilyGroup group = fgDao.readByPk(String.valueOf(groupId));
            if (group == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"error\":\"Grupo no encontrado\"}");
                return;
            }

            // Verificar que el solicitante es el dueño del grupo
            if (!group.getOwnerId().equals(ownerEmail)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"error\":\"Solo el dueño puede eliminar miembros\"}");
                return;
            }

            // No permitir que el dueño se elimine a sí mismo
            if (memberEmail.equals(ownerEmail)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\":\"No puedes eliminarte a ti mismo del grupo\"}");
                return;
            }

            // Eliminar el miembro del grupo
            boolean removed = gmDao.removeMemberFromGroup(groupId, memberEmail);
            if (!removed) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"error\":\"Miembro no encontrado en el grupo\"}");
                return;
            }

            // Limpiar la biblioteca del miembro: eliminar juegos no comprados que ya no estén en otros grupos
            cleanupLibraryForMember(memberEmail, gmDao);

            JsonObject success = new JsonObject();
            success.addProperty("message", "Miembro eliminado exitosamente");
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            try (PrintWriter out = response.getWriter()) {
                out.print(gson.toJson(success));
                out.flush();
            }

        } catch (SQLException ex) {
            Logger.getLogger(FamilyGroupMemberDeleteController.class.getName()).log(Level.SEVERE, null, ex);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Error al eliminar el miembro\"}");
        }
    }

    private void cleanupLibraryForMember(String userId, GroupMemberDao gmDao) throws SQLException {
        Connection conn = DBConnection.getInstance().getConnection();

        // Obtener otros grupos del usuario
        List<FamilyGroup> userGroups = gmDao.listGroupsByUser(userId);

        // Recopilar juegos comprados de todos los miembros de esos grupos
        List<Integer> protectedGameIds = new java.util.ArrayList<>();
        for (FamilyGroup group : userGroups) {
            List<GroupMember> members = gmDao.listMembersByGroup(group.getGroupId());
            for (GroupMember member : members) {
                String sql = "SELECT DISTINCT game_id FROM `library` WHERE user_id = ? AND buyed = TRUE";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, member.getUserId());
                java.sql.ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    int gameId = rs.getInt("game_id");
                    if (!protectedGameIds.contains(gameId)) {
                        protectedGameIds.add(gameId);
                    }
                }
                rs.close();
                pstmt.close();
            }
        }

        // Eliminar juegos no comprados del usuario, excepto los protegidos
        String deleteSql;
        PreparedStatement dstmt;
        if (protectedGameIds.isEmpty()) {
            // Sin grupos, eliminar todos los no comprados
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
        dstmt.close();
    }
}
