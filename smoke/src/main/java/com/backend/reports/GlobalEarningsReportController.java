package com.backend.reports;

import com.backend.daos.PurcharseDao;
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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servlet que proporciona el reporte de ganancias globales del sistema.
 * 
 * Muestra:
 * - Total de dinero ingresado al sistema
 * - Desglose de ganancias por empresa vs comisión de plataforma
 * - Detalle de comisión retenida de cada empresa
 */
@WebServlet(name = "GlobalEarningsReportController", urlPatterns = {"/reports/global-earnings"})
public class GlobalEarningsReportController extends HttpServlet {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            PurcharseDao purchaseDao = new PurcharseDao("purcharse", "purcharse_id");
            JsonObject earningsReport = purchaseDao.getGlobalEarningsStats();

            // Formatear el reporte
            JsonObject reportData = new JsonObject();
            reportData.addProperty("reportType", "Reporte de Ganancias Globales");
            reportData.addProperty("generatedAt", java.time.LocalDateTime.now().toString());
            reportData.add("data", earningsReport);

            response.setStatus(HttpServletResponse.SC_OK);
            try (PrintWriter out = response.getWriter()) {
                out.print(gson.toJson(reportData));
                out.flush();
            }

        } catch (SQLException ex) {
            Logger.getLogger(GlobalEarningsReportController.class.getName())
                    .log(Level.SEVERE, "Error al generar reporte de ganancias", ex);
            
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("error", "Error al generar el reporte: " + ex.getMessage());
            
            try (PrintWriter out = response.getWriter()) {
                out.print(gson.toJson(errorResponse));
                out.flush();
            }
        } catch (Exception ex) {
            Logger.getLogger(GlobalEarningsReportController.class.getName())
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
