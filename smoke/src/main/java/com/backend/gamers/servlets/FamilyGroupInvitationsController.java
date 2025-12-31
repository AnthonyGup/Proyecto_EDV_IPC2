package com.backend.gamers.servlets;

import com.backend.daos.GroupMemberDao;
import com.backend.daos.InvitationDao;
import com.backend.db.DBConnection;
import com.backend.entities.GroupMember;
import com.backend.exceptions.AlreadyExistException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet(name = "FamilyGroupInvitationsController", urlPatterns = {"/family-group/invitations"})
public class FamilyGroupInvitationsController extends HttpServlet {

    private final Gson gson = new GsonBuilder().create();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String userEmail = request.getParameter("userEmail");
        if (userEmail == null || userEmail.isBlank()) {
            userEmail = request.getParameter("userId");
        }
        if (userEmail == null || userEmail.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try (PrintWriter out = response.getWriter()) {
                out.print("{\"error\":\"userEmail requerido\"}");
                out.flush();
            }
            return;
        }

        try (PrintWriter out = response.getWriter()) {
            // Devolver invitaciones con datos del grupo (nombre y owner)
            String sql = "SELECT i.invitation_id, i.user_id, i.familyGroup_id, i.status, i.created_at, "
                    + "fg.group_name, fg.owner_id "
                    + "FROM invitation i JOIN familyGroup fg ON i.familyGroup_id = fg.group_id "
                    + "WHERE i.user_id = ? AND i.status = 'PENDING' ORDER BY i.created_at DESC";
            PreparedStatement stmt = DBConnection.getInstance().getConnection().prepareStatement(sql);
            stmt.setString(1, userEmail);
            ResultSet rs = stmt.executeQuery();
            JsonArray arr = new JsonArray();
            while (rs.next()) {
                JsonObject obj = new JsonObject();
                obj.addProperty("invitationId", rs.getInt("invitation_id"));
                obj.addProperty("userId", rs.getString("user_id"));
                obj.addProperty("familyGroupId", rs.getInt("familyGroup_id"));
                obj.addProperty("status", rs.getString("status"));
                obj.addProperty("createdAt", rs.getTimestamp("created_at").toString());
                obj.addProperty("groupName", rs.getString("group_name"));
                obj.addProperty("ownerId", rs.getString("owner_id"));
                arr.add(obj);
            }
            out.print(arr.toString());
            out.flush();
        } catch (SQLException ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = response.getWriter()) {
                out.print("{\"error\":\"Error al listar invitaciones\"}");
                out.flush();
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        JsonObject body = gson.fromJson(sb.toString(), JsonObject.class);
        if (body == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try (PrintWriter out = response.getWriter()) {
                out.print("{\"error\":\"Body requerido\"}");
                out.flush();
            }
            return;
        }

        Integer invitationId = body.has("invitationId") && !body.get("invitationId").isJsonNull() ? body.get("invitationId").getAsInt() : null;
        String action = body.has("action") && !body.get("action").isJsonNull() ? body.get("action").getAsString() : null;
        String userEmail = body.has("userEmail") && !body.get("userEmail").isJsonNull() ? body.get("userEmail").getAsString() : null;

        if (invitationId == null || action == null || userEmail == null || userEmail.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try (PrintWriter out = response.getWriter()) {
                out.print("{\"error\":\"Datos incompletos\"}");
                out.flush();
            }
            return;
        }

        InvitationDao invDao = new InvitationDao("invitation", "invitation_id");
        GroupMemberDao gmDao = new GroupMemberDao("groupMember", "");

        try {
            // Leer invitacion
            String readSql = "SELECT invitation_id, user_id, familyGroup_id FROM invitation WHERE invitation_id = ?";
            PreparedStatement rstmt = DBConnection.getInstance().getConnection().prepareStatement(readSql);
            rstmt.setInt(1, invitationId);
            ResultSet rrs = rstmt.executeQuery();
            if (!rrs.next()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                try (PrintWriter out = response.getWriter()) {
                    out.print("{\"error\":\"Invitación no encontrada\"}");
                    out.flush();
                }
                return;
            }
            String invitedUser = rrs.getString("user_id");
            int groupId = rrs.getInt("familyGroup_id");
            if (!invitedUser.equals(userEmail)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                try (PrintWriter out = response.getWriter()) {
                    out.print("{\"error\":\"No puedes gestionar esta invitación\"}");
                    out.flush();
                }
                return;
            }

            if ("accept".equalsIgnoreCase(action)) {
                // agregar como miembro y luego eliminar invitacion
                GroupMember gm = new GroupMember();
                gm.setFamilyGroupId(groupId);
                gm.setUserId(invitedUser);
                try {
                    gmDao.create(gm);
                } catch (AlreadyExistException ex) {
                    // ya es miembro, continuar a borrar invitación
                }
                // borrar invitación
                invDao.delete(String.valueOf(invitationId));
                try (PrintWriter out = response.getWriter()) {
                    out.print("{\"message\":\"Invitación aceptada\"}");
                    out.flush();
                }
                return;
            } else if ("reject".equalsIgnoreCase(action)) {
                // borrar invitación
                invDao.delete(String.valueOf(invitationId));
                try (PrintWriter out = response.getWriter()) {
                    out.print("{\"message\":\"Invitación rechazada\"}");
                    out.flush();
                }
                return;
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                try (PrintWriter out = response.getWriter()) {
                    out.print("{\"error\":\"Acción inválida\"}");
                    out.flush();
                }
                return;
            }
        } catch (SQLException ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("GROUP_LIMIT_EXCEEDED")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                try (PrintWriter out = response.getWriter()) {
                    out.print("{\"error\":\"El grupo ya tiene el máximo de 6 integrantes\"}");
                    out.flush();
                }
                return;
            }
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = response.getWriter()) {
                out.print("{\"error\":\"Error al procesar la invitación\"}");
                out.flush();
            }
        }
    }
}