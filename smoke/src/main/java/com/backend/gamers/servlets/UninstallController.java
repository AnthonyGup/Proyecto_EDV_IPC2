package com.backend.gamers.servlets;

import com.backend.daos.LibraryDao;
import com.backend.entities.Library;
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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "UninstallController", urlPatterns = {"/game/uninstall"})
public class UninstallController extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
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

            String mail = null;
            int videogameId = 0;

            JsonElement gamerElement = jsonObject.get("gamer");
            if (gamerElement != null && gamerElement.isJsonObject()) {
                JsonObject gObj = gamerElement.getAsJsonObject();
                JsonElement mailEl = gObj.get("mail");
                if (mailEl != null && !mailEl.isJsonNull()) mail = mailEl.getAsString();
            }
            if (mail == null) {
                JsonElement mailEl2 = jsonObject.get("userEmail");
                if (mailEl2 == null) mailEl2 = jsonObject.get("userId");
                if (mailEl2 != null && !mailEl2.isJsonNull()) mail = mailEl2.getAsString();
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

            final int gameIdFinal = videogameId;

            if (mail == null || gameIdFinal <= 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"message\":\"Parámetros inválidos\"}");
                return;
            }

            LibraryDao dao = new LibraryDao("`library`", "library_id");
            List<Library> libs = dao.readByGamer(mail);
            Library target = libs.stream()
                    .filter(l -> l.getGameId() == gameIdFinal)
                    .findFirst()
                    .orElse(null);

            if (target == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"message\":\"El juego no está en la biblioteca del gamer\"}");
                return;
            }

            dao.update(target.getLibraryId(), "installed", false);

            JsonObject success = new JsonObject();
            success.addProperty("message", "Juego desinstalado");
            try (PrintWriter out = response.getWriter()) {
                out.print(gson.toJson(success));
                out.flush();
            }

        } catch (SQLException ex) {
            Logger.getLogger(UninstallController.class.getName()).log(Level.SEVERE, null, ex);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"message\":\"Error de base de datos\"}");
        } catch (Exception ex) {
            Logger.getLogger(UninstallController.class.getName()).log(Level.SEVERE, null, ex);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"message\":\"Error procesando la solicitud\"}");
        }
    }
}
