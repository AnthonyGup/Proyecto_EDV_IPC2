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
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            // Leer cuerpo
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }

            String json = sb.toString();
            if (json == null || json.isBlank()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"message\":\"Body vacío\"}");
                return;
            }

            JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
            if (jsonObject == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"message\":\"JSON inválido\"}");
                return;
            }

            // Soportar dos formatos: { gamer: { mail }, game: { videogameId } } o { userEmail, videogameId }
            String mail = null;
            int videogameId = 0;

            JsonElement gamerElement = jsonObject.get("gamer");
            if (gamerElement != null && gamerElement.isJsonObject()) {
                JsonObject gObj = gamerElement.getAsJsonObject();
                JsonElement mailEl = gObj.get("mail");
                if (mailEl != null && !mailEl.isJsonNull()) {
                    mail = mailEl.getAsString();
                }
            }
            if (mail == null) {
                JsonElement mailEl2 = jsonObject.get("userEmail");
                if (mailEl2 == null) mailEl2 = jsonObject.get("userId");
                if (mailEl2 != null && !mailEl2.isJsonNull()) {
                    mail = mailEl2.getAsString();
                }
            }

            JsonElement gameElement = jsonObject.get("game");
            if (gameElement != null && gameElement.isJsonObject()) {
                JsonObject gameObj = gameElement.getAsJsonObject();
                JsonElement idEl = gameObj.get("videogameId");
                if (idEl == null) idEl = gameObj.get("gameId");
                if (idEl != null && !idEl.isJsonNull()) {
                    try { videogameId = idEl.getAsInt(); } catch (Exception ignore) {}
                }
            }
            if (videogameId <= 0) {
                JsonElement idEl2 = jsonObject.get("videogameId");
                if (idEl2 == null) idEl2 = jsonObject.get("gameId");
                if (idEl2 != null && !idEl2.isJsonNull()) {
                    try { videogameId = idEl2.getAsInt(); } catch (Exception ignore) {}
                }
            }

            if (mail == null || videogameId <= 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"message\":\"Parámetros inválidos\"}");
                return;
            }

            // Construir entidades mínimas y ejecutar instalación
            Gamer gamer = new Gamer();
            gamer.setMail(mail);
            Videogame game = new Videogame();
            game.setVideogameId(videogameId);

            Install install = new Install(gamer, game);
            try {
                install.install();
            } catch (ValidationException ex) {
                // Manejar conflicto de juegos prestados
                if (ex.getMessage() != null && ex.getMessage().startsWith("BORROWED_CONFLICT:")) {
                    String uninstalledIds = ex.getMessage().replace("BORROWED_CONFLICT:", "");
                    // Reintentar instalación después de desinstalar
                    try {
                        install.install();
                        response.setStatus(HttpServletResponse.SC_OK);
                        JsonObject successResponse = new JsonObject();
                        successResponse.addProperty("message", "Se desinstaló un juego prestado (ID: " + uninstalledIds + ") para instalar este juego");
                        try (PrintWriter out = response.getWriter()) {
                            out.print(gson.toJson(successResponse));
                            out.flush();
                        }
                        return;
                    } catch (Exception retryEx) {
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        JsonObject errorResponse = new JsonObject();
                        errorResponse.addProperty("message", "Error al reinstalar después de conflicto");
                        try (PrintWriter out = response.getWriter()) {
                            out.print(gson.toJson(errorResponse));
                            out.flush();
                        }
                        return;
                    }
                }
                
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("message", ex.getMessage());
                try (PrintWriter out = response.getWriter()) {
                    out.print(gson.toJson(errorResponse));
                    out.flush();
                }
                return;
            }

            JsonObject successResponse = new JsonObject();
            successResponse.addProperty("message", "Juego instalado exitosamente");
            try (PrintWriter out = response.getWriter()) {
                out.print(gson.toJson(successResponse));
                out.flush();
            }

        } catch (SQLException | AlreadyExistException ex) {
            Logger.getLogger(InstallController.class.getName()).log(Level.SEVERE, null, ex);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"message\":\"Error de base de datos\"}");
        } catch (Exception e) {
            Logger.getLogger(InstallController.class.getName()).log(Level.SEVERE, null, e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"message\":\"Error procesando la solicitud\"}");
        }
    }
}