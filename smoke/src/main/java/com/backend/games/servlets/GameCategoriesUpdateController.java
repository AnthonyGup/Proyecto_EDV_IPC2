package com.backend.games.servlets;

import com.backend.daos.VideogameCategoryDao;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "GameCategoriesUpdateController", urlPatterns = {"/game-categories-update"})
public class GameCategoriesUpdateController extends HttpServlet {

    private final Gson gson = new Gson();
    private final VideogameCategoryDao videogameCategoryDao = new VideogameCategoryDao("videogameCategory", "");

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            // Leer JSON del body
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                sb.append(line);
            }

            JsonObject jsonObject = JsonParser.parseString(sb.toString()).getAsJsonObject();
            int gameId = jsonObject.get("gameId").getAsInt();
            JsonArray categoriesArray = jsonObject.getAsJsonArray("categoryIds");

            Logger.getLogger(GameCategoriesUpdateController.class.getName())
                    .log(Level.INFO, "Actualizando categorias para juego {0} con {1} categorias",
                            new Object[]{gameId, categoriesArray.size()});

            // Convertir JsonArray a List<Integer>
            List<Integer> categoryIds = new ArrayList<>();
            for (int i = 0; i < categoriesArray.size(); i++) {
                categoryIds.add(categoriesArray.get(i).getAsInt());
            }

            // Usar VideogameCategoryDao para actualizar las categorias
            videogameCategoryDao.updateGameCategories(gameId, categoryIds);

            Logger.getLogger(GameCategoriesUpdateController.class.getName())
                    .log(Level.INFO, "Categorias actualizadas exitosamente para el juego {0}", gameId);

            // Respuesta exitosa
            JsonObject successResponse = new JsonObject();
            successResponse.addProperty("message", "Categorias actualizadas exitosamente");
            successResponse.addProperty("gameId", gameId);
            successResponse.addProperty("categoriesCount", categoryIds.size());

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(successResponse));
            response.getWriter().flush();

        } catch (NumberFormatException ex) {
            Logger.getLogger(GameCategoriesUpdateController.class.getName())
                    .log(Level.SEVERE, "Error en formato de datos", ex);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Formato invalido\"}");
        } catch (SQLException ex) {
            Logger.getLogger(GameCategoriesUpdateController.class.getName())
                    .log(Level.SEVERE, "Error de base de datos", ex);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Error en base de datos\"}");
        } catch (Exception ex) {
            Logger.getLogger(GameCategoriesUpdateController.class.getName())
                    .log(Level.SEVERE, "Error actualizando categorias", ex);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"" + ex.getMessage() + "\"}");
        }
    }
}