package com.backend.gamers.servlets;

import com.backend.daos.LibraryDao;
import com.backend.entities.Library;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "LibraryStatusController", urlPatterns = {"/library/status"})
public class LibraryStatusController extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String userId = request.getParameter("userId");
        String gameIdParam = request.getParameter("gameId");
        final int gameId;
        try {
            gameId = Integer.parseInt(gameIdParam);
        } catch (Exception ignore) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"message\":\"Parámetros inválidos\"}");
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if (userId == null || userId.isBlank() || gameId <= 0) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"message\":\"Parámetros inválidos\"}");
            return;
        }

        LibraryDao dao = new LibraryDao("`library`", "library_id");
        try {
            List<Library> libs = dao.readByGamer(userId);
            Library target = libs.stream()
                    .filter(l -> l.getGameId() == gameId)
                    .findFirst()
                    .orElse(null);

            JsonObject obj = new JsonObject();
            obj.addProperty("inLibrary", target != null);
            obj.addProperty("installed", target != null && target.isInstalled());
            obj.addProperty("buyed", target != null && target.isBuyed());

            try (PrintWriter out = response.getWriter()) {
                out.print(gson.toJson(obj));
                out.flush();
            }
        } catch (SQLException ex) {
            Logger.getLogger(LibraryStatusController.class.getName()).log(Level.SEVERE, null, ex);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"message\":\"Error de base de datos\"}");
        }
    }
}
