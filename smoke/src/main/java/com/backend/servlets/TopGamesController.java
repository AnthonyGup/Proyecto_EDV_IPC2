package com.backend.servlets;

import com.backend.banner.BannerManagement;
import com.backend.entities.Videogame;
import com.backend.extras.LocalDateAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Servlet para obtener los 5 juegos con mejores puntajes
 */
@WebServlet(name = "TopGamesController", urlPatterns = {"/games/top"})
public class TopGamesController extends HttpServlet {

    // Gson con adaptador para LocalDate, consistente con otros servlets
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(java.time.LocalDate.class, new LocalDateAdapter())
            .create();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        try {
            BannerManagement bannerMgmt = new BannerManagement();
            List<Videogame> topGames = bannerMgmt.getTopGames(5);

            String json = gson.toJson(topGames);
            
            response.getWriter().write(json);
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Error al obtener juegos: " + e.getMessage() + "\"}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Error inesperado: " + e.getMessage() + "\"}");
        }
    }
}
