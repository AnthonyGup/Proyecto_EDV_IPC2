/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backend.users.servlets;

import com.backend.daos.UserDao;
import com.backend.entities.User;
import com.backend.extras.LocalDateAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author antho
 */
@WebServlet(name = "Loggin", urlPatterns = {"/loggin"})
public class UserLogginController extends HttpServlet {

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
        JsonObject loginJson = gson.fromJson(json, JsonObject.class);

        String mail = loginJson.get("mail").getAsString();
        String password = loginJson.get("password").getAsString();

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try (PrintWriter out = response.getWriter()) {
            UserDao dao = new UserDao("user", "mail"); // mail es la PK
            User user = dao.readByPk(mail); // método que busca por PK

            if (user != null && user.getPassword().equals(password)) {
                JsonElement gamerJson = gson.toJsonTree(user);
                out.print(gson.toJson(gamerJson));
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
                JsonObject errorJson = new JsonObject();
                errorJson.addProperty("error", "Credenciales inválidas");
                out.print(gson.toJson(errorJson));
            }

            out.flush();
        } catch (SQLException ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = response.getWriter()) {
                JsonObject errorJson = new JsonObject();
                errorJson.addProperty("error", "Error en la base de datos: " + ex.getMessage());
                out.print(gson.toJson(errorJson));
                out.flush();
            }
        }
    }

}
