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

@WebServlet("/reports/company-sales")
public class CompanySalesReportController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String companyIdParam = request.getParameter("companyId");
        
        if (companyIdParam == null || companyIdParam.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject error = new JsonObject();
            error.addProperty("error", "companyId parameter is required");
            try (PrintWriter out = response.getWriter()) {
                out.print(error.toString());
            }
            return;
        }

        try {
            int companyId = Integer.parseInt(companyIdParam);
            PurcharseDao dao = new PurcharseDao("purcharse", "purcharse_id");
            
            JsonObject result = new JsonObject();
            result.add("sales", dao.getGameSalesByCompany(companyId));
            
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
