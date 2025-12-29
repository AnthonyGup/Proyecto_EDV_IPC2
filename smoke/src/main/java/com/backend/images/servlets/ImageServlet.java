/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backend.images.servlets;

import com.backend.daos.ImageDao;
import com.backend.daos.VideogameDao;
import com.backend.entities.Image;
import com.backend.entities.Videogame;
import com.backend.images.ImageManagement;
import com.backend.exceptions.AlreadyExistException;
import com.backend.extras.LocalDateAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
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
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author antho
 */
@WebServlet(name = "ImageServlet", urlPatterns = {"/images", "/images/*"})
public class ImageServlet extends HttpServlet {

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();

    /**
     * 
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException 
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String videogameIdParam = request.getParameter("videogame_id");
        if (videogameIdParam == null || videogameIdParam.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("message", "videogame_id es requerido");
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            try (PrintWriter out = response.getWriter()) {
                out.print(gson.toJson(errorResponse));
                out.flush();
            }
            return;
        }

        int videogameId;
        try {
            videogameId = Integer.parseInt(videogameIdParam);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("message", "videogame_id debe ser un número");
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            try (PrintWriter out = response.getWriter()) {
                out.print(gson.toJson(errorResponse));
                out.flush();
            }
            return;
        }

        ImageDao dao = new ImageDao("image", "image_id");

        try {
            List<Image> images = dao.readByGameId(videogameId);
            JsonArray imagesArray = new JsonArray();

            for (Image image : images) {
                JsonObject imageObj = new JsonObject();
                imageObj.addProperty("imageId", image.getImageId());
                imageObj.addProperty("gameId", image.getGameId());
                imageObj.addProperty("baner", image.getBaner());
                if (image.getImage() != null) {
                    String base64Image = Base64.getEncoder().encodeToString(image.getImage());
                    imageObj.addProperty("image", base64Image);
                }
                imagesArray.add(imageObj);
            }

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            try (PrintWriter out = response.getWriter()) {
                out.print(gson.toJson(imagesArray));
                out.flush();
            }

        } catch (SQLException ex) {
            Logger.getLogger(ImageServlet.class.getName()).log(Level.SEVERE, null, ex);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException 
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        JsonObject body;
        try {
            body = gson.fromJson(request.getReader(), JsonObject.class);
        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try (PrintWriter out = response.getWriter()) {
                out.print("{\"error\":\"Cuerpo JSON inválido\"}");
                out.flush();
            }
            return;
        }

        boolean baner = true; // por defecto agregar al banner
        if (body != null && body.has("baner") && !body.get("baner").isJsonNull()) {
            try { baner = body.get("baner").getAsBoolean(); } catch (Exception ignored) {}
        }

        ImageDao dao = new ImageDao("image", "image_id");
        int updates = 0;

        try {
            if (body != null && body.has("image_id") && !body.get("image_id").isJsonNull()) {
                int id = body.get("image_id").getAsInt();
                updates += dao.updateBaner(id, baner) ? 1 : 0;
            } else if (body != null && body.has("image_ids") && body.get("image_ids").isJsonArray()) {
                for (var el : body.getAsJsonArray("image_ids")) {
                    try {
                        int id = el.getAsInt();
                        updates += dao.updateBaner(id, baner) ? 1 : 0;
                    } catch (Exception ignored) {}
                }
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                try (PrintWriter out = response.getWriter()) {
                    out.print("{\"error\":\"Debe enviar 'image_id' o 'image_ids'\"}");
                    out.flush();
                }
                return;
            }

            response.setStatus(HttpServletResponse.SC_OK);
            JsonObject ok = new JsonObject();
            ok.addProperty("updated", updates);
            ok.addProperty("baner", baner);
            try (PrintWriter out = response.getWriter()) {
                out.print(gson.toJson(ok));
                out.flush();
            }
        } catch (SQLException ex) {
            Logger.getLogger(ImageServlet.class.getName()).log(Level.SEVERE, null, ex);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = response.getWriter()) {
                out.print("{\"error\":\"Error al actualizar imágenes\"}");
                out.flush();
            }
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo(); // expecting /{id}
        Integer imageId = null;
        if (pathInfo != null && pathInfo.length() > 1) {
            String[] parts = pathInfo.split("/");
            if (parts.length >= 2) {
                try { imageId = Integer.parseInt(parts[1]); } catch (NumberFormatException ex) { imageId = null; }
            }
        }
        if (imageId == null) {
            // fallback to query param
            String idParam = request.getParameter("image_id");
            if (idParam != null) {
                try { imageId = Integer.parseInt(idParam); } catch (NumberFormatException ex) { imageId = null; }
            }
        }

        if (imageId == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("message", "image_id es requerido");
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            try (PrintWriter out = response.getWriter()) {
                out.print(gson.toJson(errorResponse));
                out.flush();
            }
            return;
        }

        ImageDao dao = new ImageDao("image", "image_id");
        try {
            boolean ok = dao.delete(String.valueOf(imageId));
            response.setStatus(ok ? HttpServletResponse.SC_OK : HttpServletResponse.SC_NOT_FOUND);
            try (PrintWriter out = response.getWriter()) {
                out.print(ok ? "{\"message\":\"Imagen eliminada\"}" : "{\"error\":\"Imagen no encontrada\"}");
                out.flush();
            }
        } catch (SQLException ex) {
            Logger.getLogger(ImageServlet.class.getName()).log(Level.SEVERE, null, ex);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        JsonObject body;
        try {
            body = gson.fromJson(request.getReader(), JsonObject.class);
        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try (PrintWriter out = response.getWriter()) {
                out.print("{\"error\":\"Cuerpo JSON inválido\"}");
                out.flush();
            }
            return;
        }

        boolean baner = true; // por defecto agregar al banner
        if (body != null && body.has("baner") && !body.get("baner").isJsonNull()) {
            try { baner = body.get("baner").getAsBoolean(); } catch (Exception ignored) {}
        }

        ImageDao dao = new ImageDao("image", "image_id");
        int updates = 0;

        try {
            if (body != null && body.has("image_id") && !body.get("image_id").isJsonNull()) {
                int id = body.get("image_id").getAsInt();
                updates += dao.updateBaner(id, baner) ? 1 : 0;
            } else if (body != null && body.has("image_ids") && body.get("image_ids").isJsonArray()) {
                for (var el : body.getAsJsonArray("image_ids")) {
                    try {
                        int id = el.getAsInt();
                        updates += dao.updateBaner(id, baner) ? 1 : 0;
                    } catch (Exception ignored) {}
                }
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                try (PrintWriter out = response.getWriter()) {
                    out.print("{\"error\":\"Debe enviar 'image_id' o 'image_ids'\"}");
                    out.flush();
                }
                return;
            }

            response.setStatus(HttpServletResponse.SC_OK);
            JsonObject ok = new JsonObject();
            ok.addProperty("updated", updates);
            ok.addProperty("baner", baner);
            try (PrintWriter out = response.getWriter()) {
                out.print(gson.toJson(ok));
                out.flush();
            }
        } catch (SQLException ex) {
            Logger.getLogger(ImageServlet.class.getName()).log(Level.SEVERE, null, ex);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = response.getWriter()) {
                out.print("{\"error\":\"Error al actualizar imágenes\"}");
                out.flush();
            }
        }
    }
}