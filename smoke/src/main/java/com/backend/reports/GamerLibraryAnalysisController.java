package com.backend.reports;

import com.backend.daos.VideogameDao;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

@WebServlet("/reports/gamer-library-analysis")
public class GamerLibraryAnalysisController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");


        String userId = request.getParameter("userId");
        
        if (userId == null || userId.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject error = new JsonObject();
            error.addProperty("error", "userId parameter is required");
            try (PrintWriter out = response.getWriter()) {
                out.print(error.toString());
            }
            return;
        }

        try {
            VideogameDao dao = new VideogameDao("videogame", "videogame_id");
            
            JsonObject result = new JsonObject();
            result.add("library", dao.getGamerLibraryAnalysis(userId));
            result.add("favoriteCategories", dao.getGamerFavoriteCategories(userId));
            
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
        }
    }
}
