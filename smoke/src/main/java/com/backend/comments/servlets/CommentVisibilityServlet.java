package com.backend.comments.servlets;

import com.backend.daos.CommentDao;
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

@WebServlet(name = "CommentVisibilityServlet", urlPatterns = {"/comments/visibility"})
public class CommentVisibilityServlet extends HttpServlet {

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String companyIdParam = request.getParameter("company_id");
        String visibleParam = request.getParameter("visible");

        if (companyIdParam == null || companyIdParam.isEmpty() || visibleParam == null || visibleParam.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("message", "company_id y visible son requeridos");
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            try (PrintWriter out = response.getWriter()) {
                out.print(gson.toJson(errorResponse));
                out.flush();
            }
            return;
        }

        int companyId;
        boolean visible;

        try {
            companyId = Integer.parseInt(companyIdParam);
            visible = Boolean.parseBoolean(visibleParam);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("message", "company_id debe ser un número válido");
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            try (PrintWriter out = response.getWriter()) {
                out.print(gson.toJson(errorResponse));
                out.flush();
            }
            return;
        }

        CommentDao dao = new CommentDao("comment", "comment_id");

        try {
            dao.updateCommentVisibilityByCompany(companyId, visible);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(HttpServletResponse.SC_OK);

            JsonObject successResponse = new JsonObject();
            successResponse.addProperty("message", "Visibilidad de comentarios actualizada exitosamente");
            successResponse.addProperty("visible", visible);

            try (PrintWriter out = response.getWriter()) {
                out.print(gson.toJson(successResponse));
                out.flush();
            }

        } catch (SQLException ex) {
            Logger.getLogger(CommentVisibilityServlet.class.getName()).log(Level.SEVERE, "Error SQL al actualizar visibilidad de comentarios", ex);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("message", "Error al actualizar la visibilidad de comentarios: " + ex.getMessage());
            try (PrintWriter out = response.getWriter()) {
                out.print(gson.toJson(errorResponse));
                out.flush();
            }
        }
    }
}
