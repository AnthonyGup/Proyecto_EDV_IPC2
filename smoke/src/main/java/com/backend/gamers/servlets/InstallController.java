/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backend.gamers.servlets;

import com.backend.entities.Gamer;
import com.backend.entities.Videogame;
import com.backend.exceptions.AlreadyExistException;
import com.backend.gamers.Install;
import com.backend.validators.ValidationException;
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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author antho
 */
@WebServlet(name = "InstallController", urlPatterns = {"/game/install"})
public class InstallController extends HttpServlet {

    private final Gson gson = new Gson();

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
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        JsonElement gamerElement = jsonObject.get("gamer");
        Gamer gamer = gson.fromJson(gamerElement, Gamer.class);
        JsonElement gameElement = jsonObject.get("game");
        Videogame game = gson.fromJson(gameElement, Videogame.class);

        try {
            Install install = new Install(gamer, game);
            install.install();

        } catch (ValidationException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("message", ex.getMessage());
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            try (PrintWriter out = response.getWriter()) {
                out.print(gson.toJson(errorResponse));
                out.flush();
            }
            return;
        } catch (SQLException | AlreadyExistException ex) {
            Logger.getLogger(InstallController.class.getName()).log(Level.SEVERE, null, ex);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        JsonObject successResponse = new JsonObject();
        successResponse.addProperty("message", "Juego instalado exitosamente");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(successResponse));
            out.flush();
        } catch (Exception e) {
            Logger.getLogger(InstallController.class.getName()).log(Level.SEVERE, null, e);
        }
    }
}