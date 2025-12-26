/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backend.gamers.servlets;

import com.backend.daos.FamilyGroupDao;
import com.backend.entities.FamilyGroup;
import com.backend.exceptions.AlreadyExistException;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author antho
 */
@WebServlet(name = "FamilyGroupController", urlPatterns = {"/familygroup/creator"})
public class FamilyGroupController extends HttpServlet {

    private final Gson gson = new Gson();

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
        FamilyGroup familyGroup = gson.fromJson(json, FamilyGroup.class);

        FamilyGroupDao dao = new FamilyGroupDao("family_group", "group_id");

        try {
            dao.create(familyGroup);
        } catch (SQLException | AlreadyExistException ex) {
            Logger.getLogger(FamilyGroupController.class.getName()).log(Level.SEVERE, null, ex);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        JsonObject successResponse = new JsonObject();
        successResponse.addProperty("message", "Grupo familiar creado exitosamente");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(successResponse));
            out.flush();
        } catch (Exception e) {
            Logger.getLogger(FamilyGroupController.class.getName()).log(Level.SEVERE, null, e);
        }
    }
}