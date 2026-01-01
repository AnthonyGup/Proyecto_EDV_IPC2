package com.backend.controllers.reports;

import com.backend.daos.CommentDao;
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

@WebServlet("/reports/company-comments/export")
public class CompanyCommentsReportExportController extends HttpServlet {

    public static class GameCommentData {
        private String userId;
        private String gameTitle;
        private String commentText;
        private Integer replyCount;

        public GameCommentData() {}

        public GameCommentData(String userId, String gameTitle, String commentText, Integer replyCount) {
            this.userId = userId;
            this.gameTitle = gameTitle;
            this.commentText = commentText;
            this.replyCount = replyCount;
        }

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public String getGameTitle() { return gameTitle; }
        public void setGameTitle(String gameTitle) { this.gameTitle = gameTitle; }

        public String getCommentText() { return commentText; }
        public void setCommentText(String commentText) { this.commentText = commentText; }

        public Integer getReplyCount() { return replyCount; }
        public void setReplyCount(Integer replyCount) { this.replyCount = replyCount; }
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
            
            // Obtener datos de comentarios
            CommentDao commentDao = new CommentDao("comment", "comment_id");
            JsonArray jsonData = commentDao.getTopCommentsByCompany(companyId, limit);
            
            // Convertir JSON a POJOs
            List<GameCommentData> commentsList = new ArrayList<>();
            
            for (int i = 0; i < jsonData.size(); i++) {
                JsonObject obj = jsonData.get(i).getAsJsonObject();
                GameCommentData data = new GameCommentData(
                    obj.get("userId").getAsString(),
                    obj.get("gameTitle").getAsString(),
                    obj.get("commentText").getAsString(),
                    obj.get("replyCount").getAsInt()
                );
                commentsList.add(data);
            }
            
            // Preparar parámetros para JasperReports
            Map<String, Object> params = new HashMap<>();
            JRBeanArrayDataSource ds = new JRBeanArrayDataSource(commentsList.toArray());
            params.put("ds", ds);
            
            // Cargar y llenar reporte
            InputStream stream = getClass().getClassLoader()
                    .getResourceAsStream("reports/CompanyCommentsReport.jasper");
            
            if (stream == null) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                    "CompanyCommentsReport.jasper not found");
                return;
            }
            
            JasperPrint print = JasperFillManager.fillReport(stream, params, new JREmptyDataSource());
            
            // Exportar a PDF
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "inline; filename=comentarios_populares.pdf");
            
            JasperExportManager.exportReportToPdfStream(print, response.getOutputStream());
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid parameter format");
        } catch (JRException e) {
            throw new ServletException("Error generating PDF report", e);
        } catch (SQLException ex) {
            Logger.getLogger(CompanyCommentsReportExportController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
