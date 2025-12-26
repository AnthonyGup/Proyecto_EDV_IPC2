/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package com.backend.users.servlets;

import com.backend.daos.UserCompanyDao;
import com.backend.entities.UserCompany;
import com.backend.exceptions.AlreadyExistException;
import com.backend.extras.LocalDateAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
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

        try {
            dao.create(userCompany);
        } catch (SQLException | AlreadyExistException ex) {
            Logger.getLogger(GamerCreatorController.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Usamos el mismo gson configurado para serializar
        JsonElement jison = gson.toJsonTree(userCompany);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(jison));
            out.flush();
        } catch (Exception e) {
            
        }
    }

}
