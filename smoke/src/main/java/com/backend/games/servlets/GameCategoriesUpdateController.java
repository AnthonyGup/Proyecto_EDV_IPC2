package com.backend.games.servlets;

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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.backend.db.DBConnection;

@WebServlet(name = "GameCategoriesUpdateController", urlPatterns = {"/game-categories-update"})
public class GameCategoriesUpdateController extends HttpServlet {

    private final Gson gson = new Gson();

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
                    .log(Level.INFO, "Actualizando categor\u00edas para juego {0} con {1} categor\u00edas",
                            new Object[]{gameId, categoriesArray.size()});

            Connection conn = DBConnection.getInstance().getConnection();

            // 1. Eliminar todas las categor\u00edas actuales para este juego
            String deleteSql = "DELETE FROM videogameCategory WHERE game_id = ?";
            PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
            deleteStmt.setInt(1, gameId);
            int deletedCount = deleteStmt.executeUpdate();
            deleteStmt.close();

            Logger.getLogger(GameCategoriesUpdateController.class.getName())
                    .log(Level.INFO, "Eliminadas {0} categor\u00edas anteriores", deletedCount);

            // 2. Insertar las nuevas categor\u00edas
            String insertSql = "INSERT INTO videogameCategory (game_id, category_id) VALUES (?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertSql);

            int insertedCount = 0;
            for (int i = 0; i < categoriesArray.size(); i++) {
                int categoryId = categoriesArray.get(i).getAsInt();
                insertStmt.setInt(1, gameId);
                insertStmt.setInt(2, categoryId);
                insertStmt.executeUpdate();
                insertedCount++;

                Logger.getLogger(GameCategoriesUpdateController.class.getName())
                        .log(Level.INFO, "Categor\u00eda {0} insertada para juego {1}", new Object[]{categoryId, gameId});
            }
            insertStmt.close();

            Logger.getLogger(GameCategoriesUpdateController.class.getName())
                    .log(Level.INFO, "Insertadas {0} nuevas categor\u00edas", insertedCount);

            // Respuesta exitosa
            JsonObject successResponse = new JsonObject();
            successResponse.addProperty("message", "Categor\u00edas actualizadas exitosamente");
            successResponse.addProperty("deleted", deletedCount);
            successResponse.addProperty("inserted", insertedCount);

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(successResponse));
            response.getWriter().flush();

        } catch (NumberFormatException ex) {
            Logger.getLogger(GameCategoriesUpdateController.class.getName())
                    .log(Level.SEVERE, "Error en formato de datos", ex);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Formato inv\u00e1lido\"}");
        } catch (Exception ex) {
            Logger.getLogger(GameCategoriesUpdateController.class.getName())
                    .log(Level.SEVERE, "Error actualizando categor\u00edas", ex);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"" + ex.getMessage() + "\"}");
        }
    }
}
