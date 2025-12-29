/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backend.gamers.servlets;

import com.backend.daos.FamilyGroupDao;
import com.backend.daos.GroupMemberDao;
import com.backend.entities.FamilyGroup;
import com.backend.entities.GroupMember;
import com.backend.exceptions.AlreadyExistException;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author antho
 */
@WebServlet(name = "FamilyGroupController", urlPatterns = {"/family-group"})
public class FamilyGroupController extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String userId = request.getParameter("userId");
        String ownerId = request.getParameter("ownerId");

        if ((userId == null || userId.isBlank()) && (ownerId == null || ownerId.isBlank())) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"message\":\"Se requiere userId u ownerId\"}");
            return;
        }

        GroupMemberDao gmDao = new GroupMemberDao("groupMember", "");
        FamilyGroupDao fgDao = new FamilyGroupDao("familyGroup", "group_id");
        List<FamilyGroup> groups;
        try {
            groups = new java.util.ArrayList<>();
            if (userId != null && !userId.isBlank()) {
                groups.addAll(gmDao.listGroupsByUser(userId));
            }
            if (ownerId != null && !ownerId.isBlank()) {
                List<FamilyGroup> owned = fgDao.listByOwner(ownerId);
                for (FamilyGroup g : owned) {
                    boolean exists = groups.stream().anyMatch(existing -> existing.getGroupId() == g.getGroupId());
                    if (!exists) groups.add(g);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(FamilyGroupController.class.getName()).log(Level.SEVERE, null, ex);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(groups));
            out.flush();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }

        String json = sb.toString();
        JsonObject body = gson.fromJson(json, JsonObject.class);

        String groupName = null;
        String ownerId = null;

        if (body != null) {
            JsonElement nameEl = body.get("groupName");
            if (nameEl == null) nameEl = body.get("name");
            if (nameEl != null && !nameEl.isJsonNull()) groupName = nameEl.getAsString();

            JsonElement ownerEl = body.get("ownerId");
            if (ownerEl == null) ownerEl = body.get("userId");
            if (ownerEl == null) ownerEl = body.get("userEmail");
            if (ownerEl != null && !ownerEl.isJsonNull()) ownerId = ownerEl.getAsString();
        }

        if (groupName == null || groupName.isBlank() || ownerId == null || ownerId.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"message\":\"Parámetros inválidos\"}");
            return;
        }

        FamilyGroup familyGroup = new FamilyGroup();
        familyGroup.setGroupName(groupName);
        familyGroup.setOwnerId(ownerId);

        FamilyGroupDao dao = new FamilyGroupDao("familyGroup", "group_id");
        GroupMemberDao gmDao = new GroupMemberDao("groupMember", "");

        try {
            dao.create(familyGroup);
            FamilyGroup created = dao.readByColumn(groupName, "group_name");

            if (created != null) {
                GroupMember ownerMember = new GroupMember();
                ownerMember.setFamilyGroupId(created.getGroupId());
                ownerMember.setUserId(ownerId);
                try {
                    gmDao.create(ownerMember);
                } catch (AlreadyExistException ignored) {
                    // If already member, continue
                }
            }
        } catch (AlreadyExistException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"message\":\"Ya existe un grupo con ese nombre para este dueño\"}");
            return;
        } catch (SQLException ex) {
            Logger.getLogger(FamilyGroupController.class.getName()).log(Level.SEVERE, null, ex);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        FamilyGroup created;
        try {
            created = dao.readByColumn(groupName, "group_name");
        } catch (SQLException e) {
            created = null;
        }

        JsonObject successResponse = new JsonObject();
        successResponse.addProperty("message", "Grupo familiar creado exitosamente");
        if (created != null) successResponse.addProperty("groupId", created.getGroupId());

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(successResponse));
            out.flush();
        } catch (Exception e) {
            Logger.getLogger(FamilyGroupController.class.getName()).log(Level.SEVERE, null, e);
        }
    }
}