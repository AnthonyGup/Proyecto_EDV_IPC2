package com.backend.servlets.system;

import com.backend.categories.CategoryService;
import com.backend.entities.Category;
import com.backend.exceptions.AlreadyExistException;
import com.backend.exceptions.ServiceException;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@WebServlet(name = "CategoryController", urlPatterns = {"/category/*"})
public class CategoryController extends HttpServlet {

    private final Gson gson = new Gson();
    private final CategoryService categoryService = new CategoryService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();
        try {
            if (pathInfo != null && pathInfo.equals("/all")) {
                List<Category> categories = categoryService.listAll();
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
            Category cat = categoryService.getById(id);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(cat));
        } catch (ServiceException ex) {
            response.setStatus(ex.getStatusCode());
            response.getWriter().write("{\"error\":\"" + ex.getMessage() + "\"}");
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
            Category created = categoryService.create(incoming);
            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write(gson.toJson(created));
        } catch (ServiceException ex) {
            response.setStatus(ex.getStatusCode());
            response.getWriter().write("{\"error\":\"" + ex.getMessage() + "\"}");
        } catch (AlreadyExistException ex) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            response.getWriter().write("{\"error\":\"La categoria ya existe\"}");
        } catch (SQLException ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Error en base de datos\"}");
            Logger.getLogger(CategoryController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Error en la peticion\"}");
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
            Category updated = categoryService.updateName(id, newName);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(updated));
        } catch (ServiceException ex) {
            response.setStatus(ex.getStatusCode());
            response.getWriter().write("{\"error\":\"" + ex.getMessage() + "\"}");
        } catch (AlreadyExistException ex) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            response.getWriter().write("{\"error\":\"La categoria ya existe\"}");
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
            categoryService.delete(id);
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (ServiceException ex) {
            response.setStatus(ex.getStatusCode());
            response.getWriter().write("{\"error\":\"" + ex.getMessage() + "\"}");
        } catch (SQLException ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Error en base de datos\"}");
        }
    }
}
