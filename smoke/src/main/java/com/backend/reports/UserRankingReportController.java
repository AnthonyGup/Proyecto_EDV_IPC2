package com.backend.reports;

import com.backend.daos.CommentDao;
import com.backend.daos.PurcharseDao;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Ranking de usuarios:
 * - Usuarios con más juegos comprados (y gasto total)
 * - Usuarios con más reseñas/comentarios escritos
 */
@WebServlet(name = "UserRankingReportController", urlPatterns = {"/reports/user-ranking"})
public class UserRankingReportController extends HttpServlet {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            int limit = 10;
            String limitParam = request.getParameter("limit");
            if (limitParam != null && !limitParam.isBlank()) {
                try {
                    limit = Integer.parseInt(limitParam);
                    if (limit <= 0 || limit > 100) {
                        limit = 10;
                    }
                } catch (NumberFormatException e) {
                    limit = 10;
                }
            }

            PurcharseDao purcharseDao = new PurcharseDao("purcharse", "purcharse_id");
            CommentDao commentDao = new CommentDao("comment", "comment_id");

            JsonArray topBuyers = purcharseDao.getTopUsersByPurchases(limit);
            JsonArray topCommenters = commentDao.getTopUsersByComments(limit);

            JsonObject report = new JsonObject();
            report.addProperty("reportType", "Ranking de Usuarios");
            report.addProperty("generatedAt", java.time.LocalDateTime.now().toString());
            report.addProperty("limit", limit);
            report.add("topBuyers", topBuyers);
            report.add("topCommenters", topCommenters);

            response.setStatus(HttpServletResponse.SC_OK);
            try (PrintWriter out = response.getWriter()) {
                out.print(gson.toJson(report));
                out.flush();
            }

        } catch (SQLException ex) {
            Logger.getLogger(UserRankingReportController.class.getName())
                    .log(Level.SEVERE, "Error al generar ranking de usuarios", ex);

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("error", "Error al generar el reporte: " + ex.getMessage());

            try (PrintWriter out = response.getWriter()) {
                out.print(gson.toJson(errorResponse));
                out.flush();
            }
        } catch (Exception ex) {
            Logger.getLogger(UserRankingReportController.class.getName())
                    .log(Level.SEVERE, "Error inesperado en ranking", ex);

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("error", "Error inesperado: " + ex.getMessage());

            try (PrintWriter out = response.getWriter()) {
                out.print(gson.toJson(errorResponse));
                out.flush();
            }
        }
    }
}
