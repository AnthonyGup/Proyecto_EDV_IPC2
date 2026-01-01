package com.backend.reports;

import com.backend.daos.CommentDao;
import com.backend.daos.PurcharseDao;
import com.backend.entities.TopBuyerData;
import com.backend.entities.TopCommenterData;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanArrayDataSource;
import net.sf.jasperreports.engine.util.JRLoader;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servlet que exporta el reporte de ranking de usuarios a PDF usando JasperReports.
 */
@WebServlet(name = "UserRankingReportExportController", urlPatterns = {"/reports/user-ranking/export"})
public class UserRankingReportExportController extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        ServletOutputStream out = response.getOutputStream();

        try {
            System.out.println("=== Iniciando generación de reporte de ranking de usuarios ===");
            
            // Obtener parámetro limit
            int limit = 10;
            String limitParam = request.getParameter("limit");
            if (limitParam != null && !limitParam.isBlank()) {
                try {
                    limit = Integer.parseInt(limitParam);
                    if (limit <= 0 || limit > 100) {
                        limit = 10;
                    }
                } catch (NumberFormatException e) {
                    limit = 10;
                }
            }

            // Intentar cargar el archivo .jasper desde diferentes rutas
            InputStream reportStream = this.getServletConfig()
                    .getServletContext()
                    .getResourceAsStream("/reports/SystemUserRanking.jasper");
            
            if (reportStream == null) {
                reportStream = this.getServletConfig()
                        .getServletContext()
                        .getResourceAsStream("/WEB-INF/classes/reports/SystemUserRanking.jasper");
            }

            if (reportStream == null) {
                System.err.println("ERROR: No se encontró el archivo .jasper");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.setContentType("text/plain;charset=UTF-8");
                out.println("Error: No se encontró el archivo de reporte SystemUserRanking.jasper");
                out.flush();
                return;
            }
            
            System.out.println("✓ Archivo .jasper encontrado");

            // Obtener datos de la base de datos
            System.out.println("Obteniendo datos de top compradores y comentaristas...");
            PurcharseDao purchaseDao = new PurcharseDao("purcharse", "purcharse_id");
            CommentDao commentDao = new CommentDao("comment", "comment_id");
            
            JsonArray buyersArray = purchaseDao.getTopUsersByPurchases(limit);
            JsonArray commentersArray = commentDao.getTopUsersByComments(limit);

            System.out.println("Total compradores obtenidos: " + buyersArray.size());
            System.out.println("Total comentaristas obtenidos: " + commentersArray.size());

            // Convertir compradores a objetos Java
            List<TopBuyerData> buyersList = new ArrayList<>();
            System.out.println("Procesando " + buyersArray.size() + " compradores...");
            
            buyersList.add(new TopBuyerData());
            
            for (int i = 0; i < buyersArray.size(); i++) {
                JsonObject buyerJson = buyersArray.get(i).getAsJsonObject();
                TopBuyerData buyer = new TopBuyerData();
                
                buyer.setUserId(buyerJson.get("userId").getAsString());
                buyer.setPurchasesCount(buyerJson.get("purchasesCount").getAsInt());
                buyer.setTotalSpent(buyerJson.get("totalSpent").getAsDouble());
                buyer.setUniqueGames(buyerJson.get("uniqueGames").getAsInt());
                
                buyersList.add(buyer);
                System.out.println("  - " + buyer.getUserId() + ": " + buyer.getPurchasesCount() + " compras, $" + buyer.getTotalSpent());
            }

            // Convertir comentaristas a objetos Java
            List<TopCommenterData> commentersList = new ArrayList<>();
            System.out.println("Procesando " + commentersArray.size() + " comentaristas...");
            
            commentersList.add(new TopCommenterData());
            
            for (int i = 0; i < commentersArray.size(); i++) {
                JsonObject commenterJson = commentersArray.get(i).getAsJsonObject();
                TopCommenterData commenter = new TopCommenterData();
                
                commenter.setUserId(commenterJson.get("userId").getAsString());
                commenter.setCommentsCount(commenterJson.get("commentsCount").getAsInt());
                
                commentersList.add(commenter);
                System.out.println("  - " + commenter.getUserId() + ": " + commenter.getCommentsCount() + " comentarios");
            }

            System.out.println("Total registros compradores: " + buyersList.size());
            System.out.println("Total registros comentaristas: " + commentersList.size());

            System.out.println("Cargando reporte compilado...");
            JasperReport report = (JasperReport) JRLoader.loadObject(reportStream);
            System.out.println("✓ Reporte compilado cargado exitosamente");

            // Crear los datasources
            JRBeanArrayDataSource buyersDataSource = new JRBeanArrayDataSource(buyersList.toArray());
            JRBeanArrayDataSource commentersDataSource = new JRBeanArrayDataSource(commentersList.toArray());

            // Preparar los parámetros del reporte
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("dsBuyers", buyersDataSource);
            parameters.put("dsCommenters", commentersDataSource);

            System.out.println("Llenando reporte con datos...");
            
            // Llenar el reporte: pasar buyersDataSource como datasource principal
            JasperPrint jasperPrint = JasperFillManager.fillReport(report, parameters, buyersDataSource);
            System.out.println("✓ Reporte lleno con éxito");
            System.out.println("Número de páginas generadas: " + jasperPrint.getPages().size());

            // Configurar la respuesta HTTP para devolver un PDF
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "inline; filename=RankingUsuarios.pdf");

            System.out.println("Exportando a PDF...");
            JasperExportManager.exportReportToPdfStream(jasperPrint, out);
            
            out.flush();
            System.out.println("✓ PDF generado exitosamente");

        } catch (SQLException ex) {
            Logger.getLogger(UserRankingReportExportController.class.getName())
                    .log(Level.SEVERE, "Error SQL al obtener datos para el reporte", ex);
            System.err.println("ERROR SQL: " + ex.getMessage());
            ex.printStackTrace();

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("text/plain;charset=UTF-8");
            
            try {
                out.println("Error SQL al generar el reporte: " + ex.getMessage());
                out.flush();
            } catch (Exception e) {
                Logger.getLogger(UserRankingReportExportController.class.getName())
                        .log(Level.SEVERE, "Error escribiendo respuesta de error", e);
            }

        } catch (Exception ex) {
            Logger.getLogger(UserRankingReportExportController.class.getName())
                    .log(Level.SEVERE, "Error inesperado al generar reporte PDF", ex);
            System.err.println("ERROR: " + ex.getMessage());
            ex.printStackTrace();

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("text/plain;charset=UTF-8");
            
            try {
                out.println("Ocurrió un error al intentar generar el reporte: " + ex.getMessage());
                out.flush();
            } catch (Exception e) {
                Logger.getLogger(UserRankingReportExportController.class.getName())
                        .log(Level.SEVERE, "Error escribiendo respuesta de error", e);
            }
        }
    }
}
