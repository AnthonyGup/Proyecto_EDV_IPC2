package com.backend.reports;

import com.backend.daos.FamilyGroupDao;
import com.backend.daos.VideogameDao;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

@WebServlet("/reports/gamer-family-library")
public class GamerFamilyLibraryController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String familyGroupIdParam = request.getParameter("familyGroupId");
        String userIdParam = request.getParameter("userId");
        
        if (familyGroupIdParam == null || familyGroupIdParam.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject error = new JsonObject();
            error.addProperty("error", "familyGroupId parameter is required");
            try (PrintWriter out = response.getWriter()) {
                out.print(error.toString());
            }
            return;
        }

        try {
            int familyGroupId = Integer.parseInt(familyGroupIdParam);
            FamilyGroupDao familyDao = new FamilyGroupDao("familygroup", "familygroup_id");
            
            JsonObject result = new JsonObject();
            
            // Get all family members' shared games
            result.add("sharedGames", familyDao.getFamilySharedGames(familyGroupId));
            
            // If userId provided, get their specific library analysis within family
            if (userIdParam != null && !userIdParam.isEmpty()) {
                VideogameDao gameDao = new VideogameDao("videogame", "videogame_id");
                result.add("userLibrary", gameDao.getGamerLibraryAnalysis(userIdParam));
            }
            
            try (PrintWriter out = response.getWriter()) {
                out.print(result.toString());
            }
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject error = new JsonObject();
            error.addProperty("error", e.getMessage());
            try (PrintWriter out = response.getWriter()) {
                out.print(error.toString());
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject error = new JsonObject();
            error.addProperty("error", "Invalid familyGroupId format");
            try (PrintWriter out = response.getWriter()) {
                out.print(error.toString());
            }
        }
    }
}
