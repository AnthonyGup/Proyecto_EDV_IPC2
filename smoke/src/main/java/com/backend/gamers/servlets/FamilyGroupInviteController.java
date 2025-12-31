package com.backend.gamers.servlets;

import com.backend.daos.FamilyGroupDao;
import com.backend.daos.GroupMemberDao;
import com.backend.daos.InvitationDao;
import com.backend.entities.FamilyGroup;
import com.backend.entities.Invitation;
import com.backend.exceptions.AlreadyExistException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "FamilyGroupInviteController", urlPatterns = {"/family-group/invite"})
public class FamilyGroupInviteController extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
            response.getWriter().write("{\"message\":\"Body requerido\"}");
            return;
        }

        Integer groupId = body.has("groupId") && !body.get("groupId").isJsonNull() ? body.get("groupId").getAsInt() : null;
        String inviteeEmail = body.has("email") && !body.get("email").isJsonNull() ? body.get("email").getAsString() : null;
        if (inviteeEmail == null && body.has("userEmail") && !body.get("userEmail").isJsonNull()) {
            inviteeEmail = body.get("userEmail").getAsString();
        }
        String inviterEmail = body.has("inviter") && !body.get("inviter").isJsonNull() ? body.get("inviter").getAsString() : null;
        if (inviterEmail == null && body.has("ownerId") && !body.get("ownerId").isJsonNull()) {
            inviterEmail = body.get("ownerId").getAsString();
        }

        if (groupId == null || inviteeEmail == null || inviteeEmail.isBlank() || inviterEmail == null || inviterEmail.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"message\":\"Datos incompletos\"}");
            return;
        }

        GroupMemberDao gmDao = new GroupMemberDao("groupMember", "");
        FamilyGroupDao fgDao = new FamilyGroupDao("familyGroup", "group_id");
        InvitationDao invDao = new InvitationDao("invitation", "invitation_id");

        try {
            FamilyGroup group = fgDao.readByPk(String.valueOf(groupId));
            if (group == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"message\":\"Grupo no encontrado\"}");
                return;
            }

            // Solo el propietario del grupo puede enviar invitaciones
            if (group.getOwnerId() == null || !group.getOwnerId().equals(inviterEmail)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"message\":\"Solo el propietario del grupo puede invitar miembros\"}");
                return;
            }

            if (gmDao.isMember(inviteeEmail, groupId)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"message\":\"El usuario ya es miembro del grupo\"}");
                return;
            }

            Invitation invitation = new Invitation();
            invitation.setFamilyGroupId(groupId);
            invitation.setUserId(inviteeEmail);
            invitation.setStatus("PENDING");

            invDao.create(invitation);
        } catch (AlreadyExistException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"message\":\"Ya existe una invitación pendiente para este usuario\"}");
            return;
        } catch (SQLException ex) {
            // manejar limite de miembros
            if (ex.getMessage() != null && ex.getMessage().contains("GROUP_LIMIT_EXCEEDED")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"message\":\"El grupo ya tiene el máximo de 6 integrantes\"}");
                return;
            }
            Logger.getLogger(FamilyGroupInviteController.class.getName()).log(Level.SEVERE, null, ex);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        JsonObject success = new JsonObject();
        success.addProperty("message", "Invitación enviada");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(success));
            out.flush();
        }
    }
}
