package com.backend.reports;

import com.backend.daos.PurcharseDao;
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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Reporte de ingresos por empresa.
 * Muestra por cada empresa:
 * - Total de ventas brutas
 * - Total de comisión retenida
 * - Ganancia neta de la empresa
 * - Número de ventas
 */
@WebServlet(name = "CompanyEarningsReportController", urlPatterns = {"/reports/company-earnings"})
public class CompanyEarningsReportController extends HttpServlet {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            PurcharseDao purchaseDao = new PurcharseDao("purcharse", "purcharse_id");
            JsonObject globalStats = purchaseDao.getGlobalEarningsStats();

            JsonArray companies = globalStats.has("companiesBreakdown")
                    ? globalStats.getAsJsonArray("companiesBreakdown")
                    : new JsonArray();

            JsonObject report = new JsonObject();
            report.addProperty("reportType", "Reporte de Ingresos por Empresa");
            report.addProperty("generatedAt", java.time.LocalDateTime.now().toString());
            report.add("companies", companies);

            // Totales agregados
            if (globalStats.has("totalRevenue")) {
                report.addProperty("totalRevenue", globalStats.get("totalRevenue").getAsDouble());
            }
            if (globalStats.has("totalCompanyEarnings")) {
                report.addProperty("totalCompanyEarnings", globalStats.get("totalCompanyEarnings").getAsDouble());
            }
            if (globalStats.has("totalCommissionRetained")) {
                report.addProperty("totalCommissionRetained", globalStats.get("totalCommissionRetained").getAsDouble());
            }

            response.setStatus(HttpServletResponse.SC_OK);
            try (PrintWriter out = response.getWriter()) {
                out.print(gson.toJson(report));
                out.flush();
            }

        } catch (SQLException ex) {
            Logger.getLogger(CompanyEarningsReportController.class.getName())
                    .log(Level.SEVERE, "Error al generar reporte de ingresos por empresa", ex);

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("error", "Error al generar el reporte: " + ex.getMessage());

            try (PrintWriter out = response.getWriter()) {
                out.print(gson.toJson(errorResponse));
                out.flush();
            }
        } catch (Exception ex) {
            Logger.getLogger(CompanyEarningsReportController.class.getName())
                    .log(Level.SEVERE, "Error inesperado en reporte", ex);

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("error", "Error inesperado: " + ex.getMessage());

            try (PrintWriter out = response.getWriter()) {
                out.print(gson.toJson(errorResponse));
                out.flush();
            }
        }
    }
}
