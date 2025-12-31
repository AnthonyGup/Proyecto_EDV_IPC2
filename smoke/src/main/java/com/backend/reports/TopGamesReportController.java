package com.backend.reports;

import com.backend.daos.VideogameDao;
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
 * Servlet que proporciona el reporte de Top Ventas y Calidad.
 * 
 * Parámetros:
 * - sortBy: "sales" o "rating" (default: "sales")
 * - categoryId: ID de categoría (opcional)
 * - ageRestriction: Restricción de edad máxima (opcional)
 * - limit: Número de resultados (default: 10)
 * 
 * Retorna: Listado de juegos más vendidos y mejor valorados con opción de filtrado
 */
@WebServlet(name = "TopGamesReportController", urlPatterns = {"/reports/top-games"})
public class TopGamesReportController extends HttpServlet {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            // Obtener parámetros
            String sortBy = request.getParameter("sortBy");
            if (sortBy == null || sortBy.isBlank()) {
                sortBy = "sales"; // Default: ordenar por ventas
            }

            Integer categoryId = null;
            String categoryParam = request.getParameter("categoryId");
            if (categoryParam != null && !categoryParam.isBlank()) {
                try {
                    categoryId = Integer.parseInt(categoryParam);
                } catch (NumberFormatException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    JsonObject errorResponse = new JsonObject();
                    errorResponse.addProperty("error", "categoryId debe ser un número válido");
                    try (PrintWriter out = response.getWriter()) {
                        out.print(gson.toJson(errorResponse));
                        out.flush();
                    }
                    return;
                }
            }

            Integer ageRestriction = null;
            String ageParam = request.getParameter("ageRestriction");
            if (ageParam != null && !ageParam.isBlank()) {
                try {
                    ageRestriction = Integer.parseInt(ageParam);
                } catch (NumberFormatException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    JsonObject errorResponse = new JsonObject();
                    errorResponse.addProperty("error", "ageRestriction debe ser un número válido");
                    try (PrintWriter out = response.getWriter()) {
                        out.print(gson.toJson(errorResponse));
                        out.flush();
                    }
                    return;
                }
            }

            int limit = 10;
            String limitParam = request.getParameter("limit");
            if (limitParam != null && !limitParam.isBlank()) {
                try {
                    limit = Integer.parseInt(limitParam);
                    if (limit <= 0 || limit > 100) {
                        limit = 10; // Validar límite máximo
                    }
                } catch (NumberFormatException e) {
                    limit = 10;
                }
            }

            // Validar sortBy
            if (!sortBy.equalsIgnoreCase("sales") && !sortBy.equalsIgnoreCase("rating")) {
                sortBy = "sales";
            }

            // Obtener datos
            VideogameDao videogameDao = new VideogameDao("videogame", "videogame_id");
            JsonArray topGames = videogameDao.getTopGamesByQualityAndSales(sortBy, categoryId, ageRestriction, limit);

            // Construir reporte
            JsonObject reportData = new JsonObject();
            reportData.addProperty("reportType", "Top Ventas y Calidad");
            reportData.addProperty("generatedAt", java.time.LocalDateTime.now().toString());
            reportData.addProperty("sortedBy", sortBy.equals("sales") ? "Ventas Totales" : "Calificación Promedio");
            
            JsonObject filters = new JsonObject();
            if (categoryId != null) {
                filters.addProperty("categoryId", categoryId);
            }
            if (ageRestriction != null) {
                filters.addProperty("maxAgeRestriction", ageRestriction);
            }
            reportData.add("filters", filters);
            reportData.addProperty("resultsCount", topGames.size());
            reportData.add("games", topGames);

            response.setStatus(HttpServletResponse.SC_OK);
            try (PrintWriter out = response.getWriter()) {
                out.print(gson.toJson(reportData));
                out.flush();
            }

        } catch (SQLException ex) {
            Logger.getLogger(TopGamesReportController.class.getName())
                    .log(Level.SEVERE, "Error al generar reporte de top games", ex);
            
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("error", "Error al generar el reporte: " + ex.getMessage());
            
            try (PrintWriter out = response.getWriter()) {
                out.print(gson.toJson(errorResponse));
                out.flush();
            }
        } catch (Exception ex) {
            Logger.getLogger(TopGamesReportController.class.getName())
                    .log(Level.SEVERE, "Error inesperado en reporte", ex);
            
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
