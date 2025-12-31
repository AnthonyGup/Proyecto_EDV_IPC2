package com.backend.games.servlets;

import com.backend.entities.Videogame;
import com.backend.extras.LocalDateAdapter;
import com.backend.gamers.SearchGames;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

@WebServlet(name = "GamesSearchController", urlPatterns = {"/games/search"})
public class GamesSearchController extends HttpServlet {

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String name = param(request, "name");
        String availableParam = param(request, "available");
        String minPriceParam = param(request, "minPrice");
        String maxPriceParam = param(request, "maxPrice");
        String companyIdParam = param(request, "companyId");
        String categoryIdParam = param(request, "categoryId");
        String maxAgeParam = param(request, "maxAge");

        try {
            SearchGames service = new SearchGames();
            List<Videogame> results = service.searchByFilters(
                    name,
                    parseBoolean(availableParam),
                    parseDouble(minPriceParam),
                    parseDouble(maxPriceParam),
                    parseInt(companyIdParam),
                    parseInt(categoryIdParam),
                    parseInt(maxAgeParam)
            );
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(results));
        } catch (SQLException ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"" + ex.getMessage() + "\"}");
        }
    }

    private static String param(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        return v != null ? v.trim() : null;
    }
    private static Integer parseInt(String v) {
        try { return v != null && !v.isBlank() ? Integer.parseInt(v) : null; } catch (Exception e) { return null; }
    }
    private static Double parseDouble(String v) {
        try { return v != null && !v.isBlank() ? Double.parseDouble(v) : null; } catch (Exception e) { return null; }
    }
    private static Boolean parseBoolean(String v) {
        try { return v != null && !v.isBlank() ? Boolean.parseBoolean(v) : null; } catch (Exception e) { return null; }
    }
}
