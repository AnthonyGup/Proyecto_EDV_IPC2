/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package com.backend.servlets.system;

import com.backend.daos.CompanyDao;
import com.backend.daos.GlobalCommissionDao;
import com.backend.entities.Company;
import com.backend.entities.GlobalCommission;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author antho
 */
@WebServlet(name = "CommissionController", urlPatterns = { "/CommissionController" })
public class CommissionController extends HttpServlet {

        private final Gson gson = new Gson();

        /**
         * Handles the HTTP <code>POST</code> method.
         *
         * @param request  servlet request
         * @param response servlet response
         * @throws ServletException if a servlet-specific error occurs
         * @throws IOException      if an I/O error occurs
         */
        @Override
        protected void doPost(HttpServletRequest request, HttpServletResponse response)
                        throws ServletException, IOException {

                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");

                try {

                        String json = request.getReader().lines().collect(Collectors.joining());
                        Logger.getLogger(CommissionController.class.getName())
                                        .log(Level.INFO, "JSON recibido: {0}", json);

                        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);

                        double commission = jsonObject.get("commission").getAsDouble();
                        Company company = null;
                        if (jsonObject.has("company") && !jsonObject.get("company").isJsonNull()) {
                                company = gson.fromJson(jsonObject.get("company"), Company.class);
                        }

                        if (company != null) {
                                CompanyDao companyDao = new CompanyDao("company", "company_id");

                                companyDao.update(String.valueOf(company.getCompanyId()), "commission", commission);
                                response.setStatus(HttpServletResponse.SC_OK);
                                response.getWriter().write(
                                                "{\"message\":\"Comisión de la compañía actualizada exitosamente\"}");

                        } else {
                                GlobalCommissionDao globalDao = new GlobalCommissionDao("global_commission", "id");
                                GlobalCommission globalCommission = globalDao.readByPk("1");
                                if (globalCommission != null) {
                                        globalCommission.setCommission(commission);
                                        globalDao.create(globalCommission);
                                        updateCompanies(globalCommission);
                                        response.setStatus(HttpServletResponse.SC_OK);
                                        response.getWriter().write(
                                                        "{\"message\":\"Comisión global actualizada exitosamente\"}");
                                } else {
                                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                                        response.getWriter().write("{\"error\":\"Comisión global no encontrada\"}");
                                }
                        }

                } catch (Exception ex) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.getWriter().write("{\"error\":\"Error en la petición: " + ex.getMessage() + "\"}");
                        Logger.getLogger(CommissionController.class.getName()).log(Level.SEVERE, null, ex);
                }
        }

        /**
         * Actualiza las comisiones de las compañías que tienen una comisión mayor a la
         * comisión global.
         * @param commission la nueva comisión global
         * @throws SQLException
         */
        private void updateCompanies(GlobalCommission commission) throws SQLException {
                CompanyDao companyDao = new CompanyDao("company", "company_id");
                List<Company> companies;

                companies = companyDao.readAll();
                for (Company company : companies) {
                        if (company.getCommission() > commission.getCommission()) {
                                companyDao.update(company.getCompanyId() +  "", "commission", commission.getCommission());
                        }
                }
        }
}
