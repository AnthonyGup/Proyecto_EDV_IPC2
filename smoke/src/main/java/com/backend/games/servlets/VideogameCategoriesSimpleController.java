package com.backend.games.servlets;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.backend.db.DBConnection;

@WebServlet(name = "VideogameCategoriesSimpleController", urlPatterns = {"/game-categories"})
public class VideogameCategoriesSimpleController extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String gameIdParam = request.getParameter("gameId");
        Logger.getLogger(VideogameCategoriesSimpleController.class.getName())
                .log(Level.INFO, "Request received - gameId param: {0}", gameIdParam);

        if (gameIdParam == null || gameIdParam.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"gameId requerido\"}");
            return;
        }

        try {
            int videogameId = Integer.parseInt(gameIdParam);
            Logger.getLogger(VideogameCategoriesSimpleController.class.getName())
                    .log(Level.INFO, "Buscando categorías para videojuego: {0}", videogameId);

            Connection conn = DBConnection.getInstance().getConnection();
            
            // Query directa con JOIN
            String sql = "SELECT c.category_id, c.name " +
                        "FROM category c " +
                        "INNER JOIN videogameCategory vc ON c.category_id = vc.category_id " +
                        "WHERE vc.game_id = ?";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, videogameId);
            ResultSet rs = stmt.executeQuery();

            List<Map<String, Object>> categories = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> cat = new HashMap<>();
                cat.put("categoryId", rs.getInt("category_id"));
                cat.put("name", rs.getString("name"));
                categories.add(cat);
            }

            Logger.getLogger(VideogameCategoriesSimpleController.class.getName())
                    .log(Level.INFO, "Encontradas {0} categorías para juego {1}", new Object[]{categories.size(), videogameId});

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(categories));
            response.getWriter().flush();

        } catch (NumberFormatException ex) {
            Logger.getLogger(VideogameCategoriesSimpleController.class.getName())
                    .log(Level.SEVERE, "Error parsing ID", ex);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"ID inválido\"}");
        } catch (Exception ex) {
            Logger.getLogger(VideogameCategoriesSimpleController.class.getName())
                    .log(Level.SEVERE, "Error en consulta", ex);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"" + ex.getMessage() + "\"}");
        }
    }
}
