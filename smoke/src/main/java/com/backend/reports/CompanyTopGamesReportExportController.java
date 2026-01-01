package com.backend.reports;

import com.backend.daos.PurcharseDao;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanArrayDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/reports/company-top-games/export")
public class CompanyTopGamesReportExportController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String companyIdParam = request.getParameter("companyId");
            String startDateParam = request.getParameter("startDate");
            String endDateParam = request.getParameter("endDate");
            String limitParam = request.getParameter("limit");
            
            if (companyIdParam == null || companyIdParam.trim().isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing companyId parameter");
                return;
            }
            
            if (startDateParam == null || startDateParam.trim().isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing startDate parameter");
                return;
            }
            
            if (endDateParam == null || endDateParam.trim().isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing endDate parameter");
                return;
            }

            int companyId = Integer.parseInt(companyIdParam);
            String startDate = startDateParam;
            String endDate = endDateParam;
            int limit = 5;
            
            if (limitParam != null && !limitParam.trim().isEmpty()) {
                limit = Integer.parseInt(limitParam);
                limit = Math.max(1, Math.min(limit, 100));
            }

            System.out.println("Generating Company Top Games Report for companyId: " + companyId + 
                             ", startDate: " + startDate + ", endDate: " + endDate + ", limit: " + limit);

            // Get data from DAO
            PurcharseDao dao = new PurcharseDao("purcharse", "purcharse_id");
            JsonArray jsonData = dao.getTopGamesByCompanyInTimeRange(companyId, startDate, endDate, limit);
            System.out.println("Retrieved " + jsonData.size() + " top games records");

            // Convert to List<CompanyTopGameData>
            List<CompanyTopGameData> gamesList = new ArrayList<>();
            // Add empty record at the start (JasperReports pattern)
            gamesList.add(new CompanyTopGameData());
            
            for (int i = 0; i < jsonData.size(); i++) {
                JsonObject obj = jsonData.get(i).getAsJsonObject();
                CompanyTopGameData data = new CompanyTopGameData(
                    obj.get("gameId").getAsInt(),
                    obj.get("gameTitle").getAsString(),
                    obj.get("salesCount").getAsInt(),
                    obj.get("totalSales").getAsDouble()
                );
                gamesList.add(data);
            }

            System.out.println("Converted to " + (gamesList.size() - 1) + " CompanyTopGameData objects");

            // Create JRDataSource
            JRBeanArrayDataSource ds = new JRBeanArrayDataSource(gamesList.toArray());

            // Load JRXML template
            InputStream reportStream = this.getClass().getClassLoader()
                    .getResourceAsStream("reports/CompanyTopGamesReport.jasper");
            if (reportStream == null) {
                System.err.println("Template file not found: CompanyTopGamesReport.jasper");
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                    "Report template not found: CompanyTopGamesReport.jasper");
                return;
            }

            // Create parameters
            Map<String, Object> params = new HashMap<>();
            params.put("ds", ds);
            params.put("startDate", startDate);
            params.put("endDate", endDate);

            // Fill report
            System.out.println("Filling report...");
            JasperPrint jasperPrint = JasperFillManager.fillReport(
                reportStream,
                params,
                ds
            );

            // Send PDF response
            System.out.println("Generating PDF...");
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "inline; filename=company-top-games-report.pdf");

            OutputStream out = response.getOutputStream();
            JasperExportManager.exportReportToPdfStream(jasperPrint, out);
            out.flush();
            out.close();

            System.out.println("PDF export successful");

        } catch (NumberFormatException e) {
            System.err.println("Invalid parameters: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid parameter format");
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        } catch (JRException e) {
            System.err.println("JasperReports error: " + e.getMessage());
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Report generation error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unexpected error: " + e.getMessage());
        }
    }

    // POJO for Company Top Game Data
    public static class CompanyTopGameData {
        private int gameId;
        private String gameTitle;
        private int salesCount;
        private double totalSales;

        public CompanyTopGameData() {}

        public CompanyTopGameData(int gameId, String gameTitle, int salesCount, double totalSales) {
            this.gameId = gameId;
            this.gameTitle = gameTitle;
            this.salesCount = salesCount;
            this.totalSales = totalSales;
        }

        // Getters
        public int getGameId() { return gameId; }
        public String getGameTitle() { return gameTitle; }
        public int getSalesCount() { return salesCount; }
        public double getTotalSales() { return totalSales; }
    }
}
