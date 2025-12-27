package com.backend.users.servlets;

import com.backend.daos.UserCompanyDao;
import com.backend.entities.UserCompany;
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
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "UserCompanyController", urlPatterns = {"/user/company/info/*"})
public class UserCompanyController extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"User email requerido\"}");
            return;
        }
        
        String userEmail = pathInfo.substring(1);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try (PrintWriter out = response.getWriter()) {
            UserCompanyDao dao = new UserCompanyDao("userCompany", "user_id");
            UserCompany userCompany = dao.readByPk(userEmail);
            
            if (userCompany != null) {
                JsonObject result = new JsonObject();
                result.addProperty("company_id", userCompany.getCompany_id());
                result.addProperty("user_id", userCompany.getMail());
                out.print(gson.toJson(result));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                JsonObject errorJson = new JsonObject();
                errorJson.addProperty("error", "No se encontró relación de compañía para este usuario");
                out.print(gson.toJson(errorJson));
            }
            
            out.flush();
        } catch (SQLException ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = response.getWriter()) {
                JsonObject errorJson = new JsonObject();
                errorJson.addProperty("error", "Error en la base de datos: " + ex.getMessage());
                out.print(gson.toJson(errorJson));
                out.flush();
            }
            Logger.getLogger(UserCompanyController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
