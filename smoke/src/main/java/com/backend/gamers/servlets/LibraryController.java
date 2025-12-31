/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backend.gamers.servlets;

import com.backend.entities.Videogame;
import com.backend.entities.Library;
import com.backend.daos.LibraryDao;
import com.backend.gamers.LibraryView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
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
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author antho
 */
@WebServlet(name = "LibraryController", urlPatterns = {"/library/games"})
public class LibraryController extends HttpServlet {

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new com.backend.extras.LocalDateAdapter())
            .create();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String userId = request.getParameter("userId");
        String filterType = request.getParameter("filterType");
        String filterValue = request.getParameter("filterValue");

        if (userId == null || userId.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonElement errorResponse = gson.toJsonTree("userId es requerido");
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            try (PrintWriter out = response.getWriter()) {
                out.print(gson.toJson(errorResponse));
                out.flush();
            }
            return;
        }

        LibraryView libraryView = new LibraryView();

        try {
            List<Videogame> games = libraryView.getGamesByUserAndFilter(userId, filterType, filterValue);
            
            // Obtener info de library para agregar buyed status
            LibraryDao libDao = new LibraryDao("`library`", "library_id");
            List<Library> libs = libDao.readByGamer(userId);
            
            // Crear respuesta con metadata de buyed
            JsonArray jsonResponse = new JsonArray();
            for (Videogame game : games) {
                JsonObject gameObj = gson.toJsonTree(game).getAsJsonObject();
                Library lib = libs.stream()
                    .filter(l -> l.getGameId() == game.getVideogameId())
                    .findFirst()
                    .orElse(null);
                if (lib != null) {
                    gameObj.addProperty("buyed", lib.isBuyed());
                    gameObj.addProperty("installed", lib.isInstalled());
                }
                jsonResponse.add(gameObj);
            }

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            try (PrintWriter out = response.getWriter()) {
                out.print(gson.toJson(jsonResponse));
                out.flush();
            }

        } catch (SQLException ex) {
            Logger.getLogger(LibraryController.class.getName()).log(Level.SEVERE, null, ex);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}