package com.backend.comments.servlets;

import com.backend.daos.CommentDao;
import com.backend.exceptions.AlreadyExistException;
import com.backend.extras.LocalDateAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "GameCommentsController", urlPatterns = {"/comments/game/*"})
public class GameCommentsController extends HttpServlet {

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if (pathInfo != null && pathInfo.matches("/\\d+/status")) {
            handleGetCommentStatus(response, pathInfo);
        }
      
        else if (pathInfo != null && pathInfo.matches("/\\d+")) {
            handleGetGameComments(request, response, pathInfo);
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("message", "Formato de URL incorrecto");
            try (PrintWriter out = response.getWriter()) {
                out.print(gson.toJson(errorResponse));
                out.flush();
            }
        }
    }

    private void handleGetCommentStatus(HttpServletResponse response, String pathInfo)
            throws IOException {
        String[] parts = pathInfo.split("/");
        int videogameId = Integer.parseInt(parts[1]);

        CommentDao dao = new CommentDao("comment", "comment_id");

        try {
            // Verificar si hay comentarios visibles para este videojuego
            boolean hasVisibleComments = dao.hasVisibleCommentsByGame(videogameId);

            response.setStatus(HttpServletResponse.SC_OK);
            JsonObject successResponse = new JsonObject();
            successResponse.addProperty("enabled", hasVisibleComments);

            try (PrintWriter out = response.getWriter()) {
                out.print(gson.toJson(successResponse));
                out.flush();
            }

        } catch (SQLException ex) {
            Logger.getLogger(GameCommentsController.class.getName()).log(Level.SEVERE, 
                "Error SQL al obtener estado de comentarios", ex);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("message", "Error al obtener estado: " + ex.getMessage());
            try (PrintWriter out = response.getWriter()) {
                out.print(gson.toJson(errorResponse));
                out.flush();
            }
        }
    }

    private void handleGetGameComments(HttpServletRequest request, HttpServletResponse response, String pathInfo)
            throws IOException {
        String[] parts = pathInfo.split("/");
        int videogameId = Integer.parseInt(parts[1]);

        CommentDao dao = new CommentDao("comment", "comment_id");

        try {
            var comments = dao.readThreadedByGameId(videogameId);
            response.setStatus(HttpServletResponse.SC_OK);
            try (PrintWriter out = response.getWriter()) {
                out.print(gson.toJson(comments));
                out.flush();
            }
        } catch (SQLException ex) {
            Logger.getLogger(GameCommentsController.class.getName()).log(Level.SEVERE, 
                "Error SQL al obtener comentarios", ex);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("message", "Error al obtener comentarios: " + ex.getMessage());
            try (PrintWriter out = response.getWriter()) {
                out.print(gson.toJson(errorResponse));
                out.flush();
            }
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Formato: /comments/game/{videogameId}/disable
        if (pathInfo != null && pathInfo.matches("/\\d+/disable")) {
            handleDisableGameComments(request, response, pathInfo);
        }
        // Formato: /comments/game/{videogameId}/enable
        else if (pathInfo != null && pathInfo.matches("/\\d+/enable")) {
            handleEnableGameComments(request, response, pathInfo);
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("message", "Formato de URL incorrecto");
            try (PrintWriter out = response.getWriter()) {
                out.print(gson.toJson(errorResponse));
                out.flush();
            }
        }
    }

    private void handleDisableGameComments(HttpServletRequest request, HttpServletResponse response, String pathInfo)
            throws IOException {
        String[] parts = pathInfo.split("/");
        int videogameId = Integer.parseInt(parts[1]);

        CommentDao dao = new CommentDao("comment", "comment_id");

        try {
            dao.updateCommentVisibilityByGame(videogameId, false);

            response.setStatus(HttpServletResponse.SC_OK);
            JsonObject successResponse = new JsonObject();
            successResponse.addProperty("message", "Comentarios del juego desactivados exitosamente");

            try (PrintWriter out = response.getWriter()) {
                out.print(gson.toJson(successResponse));
                out.flush();
            }

        } catch (SQLException ex) {
            Logger.getLogger(GameCommentsController.class.getName()).log(Level.SEVERE, 
                "Error SQL al desactivar comentarios", ex);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("message", "Error al desactivar comentarios: " + ex.getMessage());
            try (PrintWriter out = response.getWriter()) {
                out.print(gson.toJson(errorResponse));
                out.flush();
            }
        }
    }

    private void handleEnableGameComments(HttpServletRequest request, HttpServletResponse response, String pathInfo)
            throws IOException {
        String[] parts = pathInfo.split("/");
        int videogameId = Integer.parseInt(parts[1]);

        CommentDao dao = new CommentDao("comment", "comment_id");

        try {
            dao.updateCommentVisibilityByGame(videogameId, true);

            response.setStatus(HttpServletResponse.SC_OK);
            JsonObject successResponse = new JsonObject();
            successResponse.addProperty("message", "Comentarios del juego activados exitosamente");

            try (PrintWriter out = response.getWriter()) {
                out.print(gson.toJson(successResponse));
                out.flush();
            }

        } catch (SQLException ex) {
            Logger.getLogger(GameCommentsController.class.getName()).log(Level.SEVERE, 
                "Error SQL al activar comentarios", ex);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("message", "Error al activar comentarios: " + ex.getMessage());
            try (PrintWriter out = response.getWriter()) {
                out.print(gson.toJson(errorResponse));
                out.flush();
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Formato: /comments/game/{videogameId}
        if (pathInfo != null && pathInfo.matches("/\\d+")) {
            String[] parts = pathInfo.split("/");
            int videogameId = Integer.parseInt(parts[1]);

            try {
                String body = request.getReader().lines().reduce("", (acc, l) -> acc + l);
                com.google.gson.JsonObject json = gson.fromJson(body, com.google.gson.JsonObject.class);
                String userId = json.has("userEmail") ? json.get("userEmail").getAsString() : null;
                String text = json.has("text") ? json.get("text").getAsString() : null;
                Integer parentId = json.has("parentId") && !json.get("parentId").isJsonNull() ? json.get("parentId").getAsInt() : null;

                if (userId == null || text == null || text.isBlank()) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    try (PrintWriter out = response.getWriter()) {
                        out.print(gson.toJson("Parámetros inválidos"));
                        out.flush();
                    }
                    return;
                }

                CommentDao dao = new CommentDao("comment", "comment_id");
                com.backend.entities.Comment c = new com.backend.entities.Comment();
                c.setUserId(userId);
                c.setGameId(videogameId);
                c.setText(text);
                c.setParentId(parentId != null ? parentId : 0);
                c.setVisible(true);
                dao.create(c);

                response.setStatus(HttpServletResponse.SC_OK);
                try (PrintWriter out = response.getWriter()) {
                    out.print(gson.toJson("Comentario agregado"));
                    out.flush();
                }
            } catch (SQLException ex) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                try (PrintWriter out = response.getWriter()) {
                    out.print(gson.toJson("Error al agregar comentario: " + ex.getMessage()));
                    out.flush();
                }
            } catch (AlreadyExistException ex) {
                Logger.getLogger(GameCommentsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try (PrintWriter out = response.getWriter()) {
                out.print(gson.toJson("Formato de URL incorrecto"));
                out.flush();
            }
        }
    }
}
