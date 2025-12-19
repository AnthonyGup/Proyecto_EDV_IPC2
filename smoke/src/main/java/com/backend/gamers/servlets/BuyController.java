/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package com.backend.gamers.servlets;

import com.backend.entities.Gamer;
import com.backend.entities.Purcharse;
import com.backend.entities.Videogame;
import com.backend.extras.LocalDateAdapter;
import com.backend.gamers.Buy;
import com.backend.validators.ValidationException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author antho
 */
@WebServlet(name = "BuyController", urlPatterns = {"/BuyController"})
public class BuyController extends HttpServlet {

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String json = request.getReader().lines().collect(Collectors.joining());

        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        Gamer gamer = gson.fromJson(root.get("gamer"), Gamer.class);
        Videogame game = gson.fromJson(root.get("videogame"), Videogame.class);

        Buy buy = new Buy(gamer, game);

        try {
            Purcharse purchase = buy.buy();

            response.setStatus(HttpServletResponse.SC_CREATED); // 201
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            //response.getWriter().write(gson.toJson(purchase));

        } catch (ValidationException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400
            response.getWriter().write("{\"error\":\"" + ex.getMessage() + "\"}");
        } catch (SQLException ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500
            response.getWriter().write("{\"error\":\"Error en base de datos\"}");
            Logger.getLogger(BuyController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
