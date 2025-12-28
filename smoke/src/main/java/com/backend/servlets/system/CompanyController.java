/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package com.backend.servlets.system;

import com.backend.daos.CompanyDao;
import com.backend.entities.Company;
import com.backend.exceptions.AlreadyExistException;
import com.backend.extras.LocalDateAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author antho
 */
@WebServlet(name = "CompanyController", urlPatterns = {"/company/create", "/company/*"})
public class CompanyController extends HttpServlet {

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        // Verificar si es solicitud de todas las compañías
        if (pathInfo != null && pathInfo.equals("/all")) {
            try {
                CompanyDao dao = new CompanyDao("company", "company_id");
                java.util.List<Company> companies = dao.readAll();
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(gson.toJson(companies));
            } catch (SQLException ex) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"error\":\"Error en base de datos\"}");
                Logger.getLogger(CompanyController.class.getName()).log(Level.SEVERE, null, ex);
            }
            return;
        }
        
        if (pathInfo == null || pathInfo.equals("/")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Company ID requerido\"}");
            return;
        }
        
        try {
            String companyId = pathInfo.substring(1); // Quita el primer "/"
            CompanyDao dao = new CompanyDao("company", "company_id");
            Company company = dao.readByPk(companyId);
            
            if (company != null) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(gson.toJson(company));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"error\":\"Company no encontrada\"}");
            }
        } catch (SQLException ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Error en base de datos\"}");
            Logger.getLogger(CompanyController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Leer JSON del cuerpo de la petición
        String json = request.getReader().lines().collect(Collectors.joining());
        Logger.getLogger(CompanyController.class.getName())
                .log(Level.INFO, "JSON recibido: {0}", json);

        // Convertir JSON a objeto Company
        Company company = gson.fromJson(json, Company.class);

        CompanyDao dao = new CompanyDao("company", "company_id");

        try {
            dao.create(company);
            // Leer la compañía creada para obtener el ID y devolverla en el body
            Company created = dao.readByColumn(company.getName(), "name");
            if (created != null) {
                response.setStatus(HttpServletResponse.SC_CREATED); // 201 Created
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(gson.toJson(created));
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"error\":\"No fue posible recuperar la compañía creada\"}");
            }
        } catch (AlreadyExistException ex) {
            response.setStatus(HttpServletResponse.SC_CONFLICT); // 409
            response.getWriter().write("{\"error\":\"Company ya existe\"}");
        } catch (SQLException ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500
            response.getWriter().write("{\"error\":\"Error en base de datos\"}");
            Logger.getLogger(CompanyController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
