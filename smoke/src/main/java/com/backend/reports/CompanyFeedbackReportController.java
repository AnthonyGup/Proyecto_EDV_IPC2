package com.backend.reports;

import com.backend.daos.RateDao;
import com.backend.daos.CommentDao;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

@WebServlet("/reports/company-feedback")
public class CompanyFeedbackReportController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String companyIdParam = request.getParameter("companyId");
        String limitParam = request.getParameter("limit");
        int limit = 10;
        
        if (limitParam != null && !limitParam.isEmpty()) {
            try {
                limit = Integer.parseInt(limitParam);
            } catch (NumberFormatException e) {
                // Use default limit
            }
        }
        
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
            RateDao rateDao = new RateDao("rate", "user_id, game_id");
            CommentDao commentDao = new CommentDao("comment", "comment_id");
            
            JsonObject result = new JsonObject();
            result.add("averageRatings", rateDao.getAverageRatingByGameForCompany(companyId));
            result.add("topComments", commentDao.getTopCommentsByCompany(companyId, limit));
            result.add("worstRated", rateDao.getWorstRatedGamesByCompany(companyId, limit));
            
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
