package com.backend.gamers.servlets;

import com.backend.daos.GroupMemberDao;
import com.backend.entities.GroupMember;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "FamilyGroupMembersController", urlPatterns = {"/family-group/members"})
public class FamilyGroupMembersController extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String groupIdStr = request.getParameter("groupId");
        if (groupIdStr == null || groupIdStr.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"message\":\"groupId es requerido\"}");
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

        GroupMemberDao gmDao = new GroupMemberDao("groupMember", "");
        List<GroupMember> members;
        try {
            members = gmDao.listMembersByGroup(groupId);
        } catch (SQLException ex) {
            Logger.getLogger(FamilyGroupMembersController.class.getName()).log(Level.SEVERE, null, ex);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(members));
            out.flush();
        }
    }
}
