/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backend.users.servlets;

import com.backend.daos.GamerDao;
import com.backend.entities.Gamer;
import com.backend.entities.enums.UserType;
import com.backend.exceptions.AlreadyExistException;
import com.backend.extras.LocalDateAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author antho
 */
@WebServlet(name = "GamerCreatorController", urlPatterns = {"/gamer/creator"})
public class GamerCreatorController extends HttpServlet {

    // Instancia única de Gson con el adaptador de LocalDate
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();

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
        Gamer gamer = gson.fromJson(json, Gamer.class);
        gamer.setType(UserType.GAMER);
        GamerDao dao = new GamerDao("gamer", "user_id");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            dao.create(gamer);
            // Éxito: retorna 200 con el gamer creado
            response.setStatus(HttpServletResponse.SC_OK);
            JsonElement jison = gson.toJsonTree(gamer);
            try (PrintWriter out = response.getWriter()) {
                out.print(gson.toJson(jison));
                out.flush();
            }
        } catch (AlreadyExistException ex) {
            Logger.getLogger(GamerCreatorController.class.getName()).log(Level.WARNING, "Usuario ya existe: " + gamer.getMail());
            response.setStatus(HttpServletResponse.SC_CONFLICT); // 409
            try (PrintWriter out = response.getWriter()) {
                out.print("{\"error\":\"El correo ya está registrado\"}");
                out.flush();
            }
        } catch (SQLException ex) {
            Logger.getLogger(GamerCreatorController.class.getName()).log(Level.SEVERE, "Error al crear gamer", ex);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500
            try (PrintWriter out = response.getWriter()) {
                out.print("{\"error\":\"Error al registrar usuario\"}");
                out.flush();
            }
        }
    }
}
