package com.backend.servlets.system;

import com.backend.daos.CategoryDao;
import com.backend.entities.Category;
import com.backend.exceptions.AlreadyExistException;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@WebServlet(name = "CategoryController", urlPatterns = {"/category/*"})
public class CategoryController extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();
        try {
            com.backend.daos.CategoryDao dao = new com.backend.daos.CategoryDao("category", "category_id");
            if (pathInfo != null && pathInfo.equals("/all")) {
                java.util.List<Category> categories = dao.readAll();
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(gson.toJson(categories));
                return;
            }

            if (pathInfo == null || pathInfo.equals("/")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\":\"Category ID requerido\"}");
                return;
            }

            String id = pathInfo.substring(1);
            Category cat = dao.readByPk(id);
            if (cat != null) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(gson.toJson(cat));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"error\":\"Categoría no encontrada\"}");
            }
        } catch (SQLException ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Error en base de datos\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            String json = request.getReader().lines().collect(Collectors.joining());
            Logger.getLogger(CategoryController.class.getName())
                    .log(Level.INFO, "JSON recibido: {0}", json);

            Category incoming = gson.fromJson(json, Category.class);
            if (incoming == null || incoming.getName() == null || incoming.getName().isBlank()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\":\"Nombre de categoría requerido\"}");
                return;
            }

            CategoryDao dao = new CategoryDao("category", "category_id");
            dao.create(incoming);

            Category created = dao.readByColumn(incoming.getName(), "name");
            if (created != null) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                response.getWriter().write(gson.toJson(created));
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"error\":\"No fue posible recuperar la categoría creada\"}");
            }
        } catch (AlreadyExistException ex) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            response.getWriter().write("{\"error\":\"La categoría ya existe\"}");
        } catch (SQLException ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Error en base de datos\"}");
            Logger.getLogger(CategoryController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Error en la petición\"}");
            Logger.getLogger(CategoryController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Category ID requerido\"}");
            return;
        }
        String id = pathInfo.substring(1);
        try {
            String json = request.getReader().lines().collect(java.util.stream.Collectors.joining());
            Category body = gson.fromJson(json, Category.class);
            String newName = body != null ? body.getName() : null;
            if (newName == null || newName.isBlank()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\":\"Nombre de categoría requerido\"}");
                return;
            }
            com.backend.daos.CategoryDao dao = new com.backend.daos.CategoryDao("category", "category_id");
            // Evitar duplicados
            Category existing = dao.readByColumn(newName, "name");
            if (existing != null && !(String.valueOf(existing.getCategoryId()).equals(id))) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                response.getWriter().write("{\"error\":\"La categoría ya existe\"}");
                return;
            }
            dao.update(id, "name", newName);
            Category updated = dao.readByPk(id);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(updated));
        } catch (SQLException ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Error en base de datos\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Category ID requerido\"}");
            return;
        }
        String id = pathInfo.substring(1);
        try {
            com.backend.daos.CategoryDao dao = new com.backend.daos.CategoryDao("category", "category_id");
            dao.delete(id);
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (SQLException ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Error en base de datos\"}");
        }
    }
}
