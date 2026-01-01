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

@WebServlet("/reports/company-sales/export")
public class CompanySalesReportExportController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String companyIdParam = request.getParameter("companyId");
            if (companyIdParam == null || companyIdParam.trim().isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing companyId parameter");
                return;
            }

            int companyId = Integer.parseInt(companyIdParam);
            System.out.println("Generating Company Sales Report for companyId: " + companyId);

            // Get data from DAO
            PurcharseDao dao = new PurcharseDao("purcharse", "purcharse_id");
            JsonArray jsonData = dao.getGameSalesByCompany(companyId);
            System.out.println("Retrieved " + jsonData.size() + " game sales records");

            // Convert to List<CompanySalesData>
            List<CompanySalesData> salesList = new ArrayList<>();
            // Add empty record at the start (JasperReports pattern)
            salesList.add(new CompanySalesData());
            
            for (int i = 0; i < jsonData.size(); i++) {
                JsonObject obj = jsonData.get(i).getAsJsonObject();
                CompanySalesData data = new CompanySalesData(
                    obj.get("gameId").getAsInt(),
                    obj.get("gameTitle").getAsString(),
                    obj.get("salesCount").getAsInt(),
                    obj.get("totalSales").getAsDouble(),
                    obj.get("commissionPercentage").getAsDouble(),
                    obj.get("netEarnings").getAsDouble()
                );
                salesList.add(data);
            }

            System.out.println("Converted to " + (salesList.size() - 1) + " CompanySalesData objects");

            // Create JRDataSource
            JRBeanArrayDataSource ds = new JRBeanArrayDataSource(salesList.toArray());

            // Load JRXML template
            InputStream reportStream = this.getClass().getClassLoader()
                    .getResourceAsStream("reports/CompanySalesReport.jasper");
            if (reportStream == null) {
                System.err.println("Template file not found: CompanySalesReport.jasper");
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                    "Report template not found: CompanySalesReport.jasper");
                return;
            }

            // Create parameters
            Map<String, Object> params = new HashMap<>();
            params.put("ds", ds);

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
            response.setHeader("Content-Disposition", "inline; filename=company-sales-report.pdf");

            OutputStream out = response.getOutputStream();
            JasperExportManager.exportReportToPdfStream(jasperPrint, out);
            out.flush();
            out.close();

            System.out.println("PDF export successful");

        } catch (NumberFormatException e) {
            System.err.println("Invalid companyId: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid companyId format");
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

    // POJO for Company Sales Data
    public static class CompanySalesData {
        private int gameId;
        private String gameTitle;
        private int salesCount;
        private double totalSales;
        private double commissionPercentage;
        private double netEarnings;

        public CompanySalesData() {}

        public CompanySalesData(int gameId, String gameTitle, int salesCount, 
                               double totalSales, double commissionPercentage, double netEarnings) {
            this.gameId = gameId;
            this.gameTitle = gameTitle;
            this.salesCount = salesCount;
            this.totalSales = totalSales;
            this.commissionPercentage = commissionPercentage;
            this.netEarnings = netEarnings;
        }

        // Getters
        public int getGameId() { return gameId; }
        public String getGameTitle() { return gameTitle; }
        public int getSalesCount() { return salesCount; }
        public double getTotalSales() { return totalSales; }
        public double getCommissionPercentage() { return commissionPercentage; }
        public double getNetEarnings() { return netEarnings; }
    }
}
