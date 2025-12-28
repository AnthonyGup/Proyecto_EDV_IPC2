package com.backend.games.servlets;

import com.backend.daos.CategoryDao;
import com.backend.daos.VideogameCategoryDao;
import com.backend.daos.VideogameDao;
import com.backend.entities.Category;
import com.backend.entities.Videogame;
import com.backend.entities.VideogameCategory;
import com.backend.images.ImageManagement;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "VideogameCategoriesController", urlPatterns = {"/videogame/*"})
@MultipartConfig
public class VideogameCategoriesController extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();
        Logger.getLogger(VideogameCategoriesController.class.getName())
                .log(Level.INFO, "Request pathInfo: {0}", pathInfo);
        
        if (pathInfo == null || pathInfo.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Videogame ID requerido\"}");
            return;
        }

        // Only handle /videogame/{id}/categories requests
        if (!pathInfo.contains("/categories")) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("{\"error\":\"Ruta no encontrada\"}");
            return;
        }

        // Extract videogame ID from /videogame/{id}/categories
        // pathInfo will be /1/categories, split gives ["", "1", "categories"]
        String[] parts = pathInfo.split("/");
        Logger.getLogger(VideogameCategoriesController.class.getName())
                .log(Level.INFO, "Path parts length: {0}", parts.length);
        
        if (parts.length < 3) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Formato inválido\"}");
            return;
        }

        try {
            int videogameId = Integer.parseInt(parts[1]);
            Logger.getLogger(VideogameCategoriesController.class.getName())
                    .log(Level.INFO, "Buscando categorías para videojuego ID: {0}", videogameId);
            
            VideogameCategoryDao videoGameCatDao = new VideogameCategoryDao("videogame_category", "category_id");
            List<VideogameCategory> videoGameCategories = videoGameCatDao.readByGameId(videogameId);
            
            Logger.getLogger(VideogameCategoriesController.class.getName())
                    .log(Level.INFO, "Encontradas {0} relaciones videojuego-categoría", videoGameCategories.size());

            if (videoGameCategories.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(gson.toJson(new ArrayList<>()));
                return;
            }

            CategoryDao catDao = new CategoryDao("category", "category_id");
            List<Category> categories = new ArrayList<>();
            for (VideogameCategory vc : videoGameCategories) {
                try {
                    Category cat = catDao.readByPk(String.valueOf(vc.getCategoryId()));
                    if (cat != null) {
                        categories.add(cat);
                        Logger.getLogger(VideogameCategoriesController.class.getName())
                                .log(Level.INFO, "Categoría agregada: {0}", cat.getName());
                    }
                } catch (SQLException e) {
                    Logger.getLogger(VideogameCategoriesController.class.getName())
                            .log(Level.WARNING, "Error al obtener categoría ID: " + vc.getCategoryId(), e);
                }
            }

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(categories));
        } catch (NumberFormatException ex) {
            Logger.getLogger(VideogameCategoriesController.class.getName())
                    .log(Level.SEVERE, "Error parsing videogame ID: " + parts[1], ex);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"ID de videojuego inválido: " + parts[1] + "\"}");
        } catch (SQLException ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Error en base de datos\"}");
            Logger.getLogger(VideogameCategoriesController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Videogame ID requerido\"}");
            return;
        }

        String[] parts = pathInfo.split("/");
        if (parts.length < 2) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Formato inválido\"}");
            return;
        }

        int videogameId;
        try {
            videogameId = Integer.parseInt(parts[1]);
        } catch (NumberFormatException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"ID de videojuego inválido\"}");
            return;
        }

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }

        JsonObject body = JsonParser.parseString(sb.toString()).getAsJsonObject();
        VideogameDao dao = new VideogameDao("videogame", "videogame_id");

        try {
            if (body.has("name")) dao.update(String.valueOf(videogameId), "name", body.get("name").getAsString());
            if (body.has("description")) dao.update(String.valueOf(videogameId), "description", body.get("description").getAsString());
            if (body.has("price")) dao.update(String.valueOf(videogameId), "price", body.get("price").getAsDouble());
            if (body.has("ageRestriction")) dao.update(String.valueOf(videogameId), "age_restriction", body.get("ageRestriction").getAsInt());
            if (body.has("minimRequirements")) dao.update(String.valueOf(videogameId), "min_requirements", body.get("minimRequirements").getAsString());
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("{\"message\":\"Videojuego actualizado correctamente\"}");
        } catch (SQLException ex) {
            Logger.getLogger(VideogameCategoriesController.class.getName()).log(Level.SEVERE, null, ex);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Error en base de datos\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Videogame ID requerido\"}");
            return;
        }

        String[] parts = pathInfo.split("/");
        if (parts.length < 3) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Formato inválido\"}");
            return;
        }

        int videogameId;
        try {
            videogameId = Integer.parseInt(parts[1]);
        } catch (NumberFormatException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"ID de videojuego inválido\"}");
            return;
        }

        String action = parts[2];
        if ("availability".equals(action)) {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
            JsonObject body = JsonParser.parseString(sb.toString()).getAsJsonObject();
            boolean available = body.has("available") && body.get("available").getAsBoolean();
            VideogameDao dao = new VideogameDao("videogame", "videogame_id");
            try {
                dao.update(String.valueOf(videogameId), "available", available);
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("{\"message\":\"Disponibilidad actualizada\"}");
            } catch (SQLException ex) {
                Logger.getLogger(VideogameCategoriesController.class.getName()).log(Level.SEVERE, null, ex);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"error\":\"Error en base de datos\"}");
            }
            return;
        }

        if ("images".equals(action)) {
            // handle multipart file upload: parts named "files"
            Videogame game = new Videogame();
            game.setVideogameId(videogameId);
            ImageManagement imageManagement = new ImageManagement();
            try {
                Collection<Part> partsCollection = request.getParts();
                for (Part part : partsCollection) {
                    if ("files".equals(part.getName()) && part.getSize() > 0) {
                        byte[] bytes = part.getInputStream().readAllBytes();
                        com.backend.entities.Image img = new com.backend.entities.Image();
                        img.setImage(bytes);
                        imageManagement.addImage(game, img);
                    }
                }
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("{\"message\":\"Imágenes subidas correctamente\"}");
            } catch (Exception ex) {
                Logger.getLogger(VideogameCategoriesController.class.getName()).log(Level.SEVERE, null, ex);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"error\":\"Error al subir imágenes\"}");
            }
            return;
        }

        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.getWriter().write("{\"error\":\"Ruta no encontrada\"}");
    }
}
