/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package com.backend.users.servlets;

import com.backend.daos.UserCompanyDao;
import com.backend.daos.UserDao;
import com.backend.entities.UserCompany;
import com.backend.exceptions.AlreadyExistException;
import com.backend.extras.LocalDateAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author antho
 */
@WebServlet(name = "CompanyAdminController", urlPatterns = {"/user/company"})
public class CompanyAdminController extends HttpServlet {

    // Instancia única de Gson con el adaptador de LocalDate
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }

        String json = sb.toString();
        //System.out.println("JSON recibido: [" + json + "]"); 
        //System.out.println("Longitud: " + json.length());    
        UserCompany userCompany = gson.fromJson(json, UserCompany.class);

        UserCompanyDao dao = new UserCompanyDao("userCompany", "user_id");
        UserDao userDao =  new UserDao("user",  "mail");
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            // Verificar si el email ya existe
            if (userDao.readByPk(userCompany.getMail()) != null) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                response.getWriter().write("{\"error\":\"El email ya está registrado en el sistema\"}");
                return;
            }
            
            // Crear el usuario si el email es único
            dao.create(userCompany);
            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write(gson.toJson(userCompany));
        } catch (AlreadyExistException ex) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            response.getWriter().write("{\"error\":\"El usuario ya existe\"}");
            Logger.getLogger(CompanyAdminController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Error en base de datos\"}");
            Logger.getLogger(CompanyAdminController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
