package com.backend.reports;

import com.backend.daos.PurcharseDao;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

@WebServlet("/reports/company-top-games")
public class CompanyTopGamesReportController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String companyIdParam = request.getParameter("companyId");
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");
        String limitParam = request.getParameter("limit");
        int limit = 5;
        
        if (limitParam != null && !limitParam.isEmpty()) {
            try {
                limit = Integer.parseInt(limitParam);
                if (limit > 100) limit = 100;
                if (limit < 1) limit = 1;
            } catch (NumberFormatException e) {
                // Use default
            }
        }
        
        if (companyIdParam == null || companyIdParam.isEmpty() || 
            startDate == null || startDate.isEmpty() || 
            endDate == null || endDate.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject error = new JsonObject();
            error.addProperty("error", "companyId, startDate and endDate parameters are required");
            try (PrintWriter out = response.getWriter()) {
                out.print(error.toString());
            }
            return;
        }

        try {
            int companyId = Integer.parseInt(companyIdParam);
            PurcharseDao dao = new PurcharseDao("purcharse", "purcharse_id");
            
            JsonObject result = new JsonObject();
            result.add("topGames", dao.getTopGamesByCompanyInTimeRange(companyId, startDate, endDate, limit));
            
            try (PrintWriter out = response.getWriter()) {
                out.print(result.toString());
            }
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject error = new JsonObject();
            error.addProperty("error", e.getMessage());
            try (PrintWriter out = response.getWriter()) {
                out.print(error.toString());
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject error = new JsonObject();
            error.addProperty("error", "Invalid companyId format");
            try (PrintWriter out = response.getWriter()) {
                out.print(error.toString());
            }
        }
    }
}
