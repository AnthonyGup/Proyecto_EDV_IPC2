package com.backend.rate.servlets;

import com.backend.daos.RateDao;
import com.backend.db.DBConnection;
import com.backend.daos.LibraryDao;
import com.backend.entities.Library;
import com.backend.entities.Rate;
import com.backend.exceptions.AlreadyExistException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

@WebServlet(name = "GameRateController", urlPatterns = {"/rate/game/*"})
public class GameRateController extends HttpServlet {

    private final Gson gson = new GsonBuilder().create();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || !pathInfo.matches("/\\d+")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try (PrintWriter out = response.getWriter()) {
                out.print("{\"error\":\"Formato de URL incorrecto\"}");
                out.flush();
            }
            return;
        }
        int videogameId = Integer.parseInt(pathInfo.substring(1));

        String body = request.getReader().lines().reduce("", (a, b) -> a + b);
        com.google.gson.JsonObject json = gson.fromJson(body, com.google.gson.JsonObject.class);
        String userEmail = json.has("userEmail") ? json.get("userEmail").getAsString() : null;
        Integer stars = json.has("stars") ? json.get("stars").getAsInt() : null;

        if (userEmail == null || stars == null || stars < 0 || stars > 5) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try (PrintWriter out = response.getWriter()) {
                out.print("{\"error\":\"Parámetros inválidos\"}");
                out.flush();
            }
            return;
        }

        RateDao dao = new RateDao("rate", "user_id");
        // Verificar que el usuario haya comprado el juego
        try {
            LibraryDao libDao = new LibraryDao("`library`", "library_id");
            java.util.List<Library> libs = libDao.readByGamer(userEmail);
            Library target = libs.stream().filter(l -> l.getGameId() == videogameId).findFirst().orElse(null);
            if (target == null || !target.isBuyed()) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                try (PrintWriter out = response.getWriter()) {
                    out.print("{\"error\":\"Solo puedes calificar juegos comprados\"}");
                    out.flush();
                }
                return;
            }
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = response.getWriter()) {
                out.print("{\"error\":\"Error al verificar compra\"}");
                out.flush();
            }
            return;
        }
        try {
            Rate r = new Rate();
            r.setUserId(userEmail);
            r.setGameId(videogameId);
            r.setStars(stars);
            dao.create(r);
            response.setStatus(HttpServletResponse.SC_OK);
            try (PrintWriter out = response.getWriter()) {
                out.print("{\"message\":\"Calificación registrada\"}");
                out.flush();
            }
        } catch (AlreadyExistException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try (PrintWriter out = response.getWriter()) {
                out.print("{\"error\":\"Ya calificaste este juego\"}");
                out.flush();
            }
        } catch (SQLException ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = response.getWriter()) {
                out.print("{\"error\":\"Error al registrar calificación: " + ex.getMessage() + "\"}");
                out.flush();
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || !pathInfo.matches("/\\d+")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try (PrintWriter out = response.getWriter()) {
                out.print("{\"error\":\"Formato de URL incorrecto\"}");
                out.flush();
            }
            return;
        }
        int videogameId = Integer.parseInt(pathInfo.substring(1));

        String userEmail = request.getParameter("userEmail");

        try (PrintWriter out = response.getWriter()) {
            if (userEmail != null && !userEmail.isBlank()) {
                // devolver la calificación del usuario
                String sql = "SELECT stars FROM rate WHERE user_id = ? AND game_id = ? LIMIT 1";
                java.sql.PreparedStatement stmt = DBConnection.getInstance().getConnection().prepareStatement(sql);
                stmt.setString(1, userEmail);
                stmt.setInt(2, videogameId);
                java.sql.ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    out.print("{\"rating\":" + rs.getInt("stars") + "}");
                } else {
                    out.print("{\"rating\":null}");
                }
                out.flush();
            } else {
                // devolver promedio del juego
                String sql = "SELECT AVG(stars) AS avg_rating, COUNT(*) AS total FROM rate WHERE game_id = ?";
                java.sql.PreparedStatement stmt = DBConnection.getInstance().getConnection().prepareStatement(sql);
                stmt.setInt(1, videogameId);
                java.sql.ResultSet rs = stmt.executeQuery();
                double avg = 0.0;
                int total = 0;
                if (rs.next()) {
                    avg = rs.getDouble("avg_rating");
                    total = rs.getInt("total");
                }
                out.print("{\"average\":" + avg + ",\"count\":" + total + "}");
                out.flush();
            }
        } catch (SQLException ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = response.getWriter()) {
                out.print("{\"error\":\"Error al obtener calificación\"}");
                out.flush();
            }
        }
    }
}
