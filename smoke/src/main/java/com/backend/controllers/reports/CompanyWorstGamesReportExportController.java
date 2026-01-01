package com.backend.controllers.reports;

import com.backend.daos.RateDao;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanArrayDataSource;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/reports/company-worst-games/export")
public class CompanyWorstGamesReportExportController extends HttpServlet {

    public static class WorstGameData {
        private String gameTitle;
        private Double averageRating;
        private Integer ratingCount;

        public WorstGameData() {}

        public WorstGameData(String gameTitle, Double averageRating, Integer ratingCount) {
            this.gameTitle = gameTitle;
            this.averageRating = averageRating;
            this.ratingCount = ratingCount;
        }

        public String getGameTitle() { return gameTitle; }
        public void setGameTitle(String gameTitle) { this.gameTitle = gameTitle; }

        public Double getAverageRating() { return averageRating; }
        public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }

        public Integer getRatingCount() { return ratingCount; }
        public void setRatingCount(Integer ratingCount) { this.ratingCount = ratingCount; }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String companyIdParam = request.getParameter("companyId");
        String limitParam = request.getParameter("limit");
        
        if (companyIdParam == null || companyIdParam.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "companyId parameter is required");
            return;
        }

        try {
            int companyId = Integer.parseInt(companyIdParam);
            int limit = (limitParam != null && !limitParam.isEmpty()) ? 
                Integer.parseInt(limitParam) : 10;
            
            // Obtener datos de juegos peor calificados
            RateDao rateDao = new RateDao("rate", "rate_id");
            JsonArray jsonData = rateDao.getWorstRatedGamesByCompany(companyId, limit);
            
            // Convertir JSON a POJOs
            List<WorstGameData> worstGamesList = new ArrayList<>();
            
            for (int i = 0; i < jsonData.size(); i++) {
                JsonObject obj = jsonData.get(i).getAsJsonObject();
                WorstGameData data = new WorstGameData(
                    obj.get("gameTitle").getAsString(),
                    obj.get("averageRating").getAsDouble(),
                    obj.get("ratingCount").getAsInt()
                );
                worstGamesList.add(data);
            }
            
            // Preparar parámetros para JasperReports
            Map<String, Object> params = new HashMap<>();
            JRBeanArrayDataSource ds = new JRBeanArrayDataSource(worstGamesList.toArray());
            params.put("ds", ds);
            
            // Cargar y llenar reporte
            InputStream stream = getClass().getClassLoader()
                    .getResourceAsStream("reports/CompanyWorstGamesReport.jasper");
            
            if (stream == null) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                    "CompanyWorstGamesReport.jasper not found");
                return;
            }
            
            JasperPrint print = JasperFillManager.fillReport(stream, params, new JREmptyDataSource());
            
            // Exportar a PDF
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "inline; filename=juegos_peor_calificados.pdf");
            
            JasperExportManager.exportReportToPdfStream(print, response.getOutputStream());
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid parameter format");
        } catch (JRException e) {
            throw new ServletException("Error generating PDF report", e);
        } catch (SQLException ex) {
            Logger.getLogger(CompanyWorstGamesReportExportController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
