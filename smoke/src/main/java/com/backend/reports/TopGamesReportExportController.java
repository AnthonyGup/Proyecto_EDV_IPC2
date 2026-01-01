package com.backend.reports;

import com.backend.daos.VideogameDao;
import com.backend.entities.TopGameData;
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
 * Servlet que exporta el reporte de juegos más vendidos y mejor valorados a PDF usando JasperReports.
 */
@WebServlet(name = "TopGamesReportExportController", urlPatterns = {"/reports/top-games/export"})
public class TopGamesReportExportController extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        ServletOutputStream out = response.getOutputStream();

        try {
            System.out.println("=== Iniciando generación de reporte de juegos más vendidos ===");
            
            // Obtener parámetros
            String sortBy = request.getParameter("sortBy");
            if (sortBy == null || sortBy.isBlank()) {
                sortBy = "sales";
            }

            Integer categoryId = null;
            String categoryParam = request.getParameter("categoryId");
            if (categoryParam != null && !categoryParam.isBlank()) {
                try {
                    categoryId = Integer.parseInt(categoryParam);
                } catch (NumberFormatException e) {
                    // Ignore, use null
                }
            }

            Integer ageRestriction = null;
            String ageParam = request.getParameter("ageRestriction");
            if (ageParam != null && !ageParam.isBlank()) {
                try {
                    ageRestriction = Integer.parseInt(ageParam);
                } catch (NumberFormatException e) {
                    // Ignore, use null
                }
            }

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
                    .getResourceAsStream("/reports/SystemTopGames.jasper");
            
            if (reportStream == null) {
                reportStream = this.getServletConfig()
                        .getServletContext()
                        .getResourceAsStream("/WEB-INF/classes/reports/SystemTopGames.jasper");
            }

            if (reportStream == null) {
                System.err.println("ERROR: No se encontró el archivo .jasper");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.setContentType("text/plain;charset=UTF-8");
                out.println("Error: No se encontró el archivo de reporte SystemTopGames.jasper");
                out.flush();
                return;
            }
            
            System.out.println("✓ Archivo .jasper encontrado");

            // Obtener datos de la base de datos
            System.out.println("Obteniendo datos de los juegos más vendidos...");
            VideogameDao videogameDao = new VideogameDao("videogame", "videogame_id");
            JsonArray gamesArray = videogameDao.getTopGamesByQualityAndSales(sortBy, categoryId, ageRestriction, limit);

            System.out.println("Total juegos obtenidos: " + gamesArray.size());

            // Convertir a objetos Java
            List<TopGameData> gamesList = new ArrayList<>();
            System.out.println("Procesando " + gamesArray.size() + " juegos...");
            
            // IMPORTANTE: Agregar un registro vacío al inicio
            gamesList.add(new TopGameData());
            
            for (int i = 0; i < gamesArray.size(); i++) {
                JsonObject gameJson = gamesArray.get(i).getAsJsonObject();
                TopGameData game = new TopGameData();
                
                game.setName(gameJson.get("name").getAsString());
                game.setCompanyName(gameJson.get("companyName").getAsString());
                game.setPrice(gameJson.get("price").getAsDouble());
                game.setTotalSales(gameJson.get("totalSales").getAsInt());
                game.setAvgRating(gameJson.get("avgRating").getAsDouble());
                game.setRatingCount(gameJson.get("ratingCount").getAsInt());
                
                gamesList.add(game);
                System.out.println("  - " + game.getName() + " (" + game.getCompanyName() + "): " + game.getTotalSales() + " ventas, Rating: " + game.getAvgRating());
            }

            System.out.println("Total registros con vacío inicial: " + gamesList.size());

            System.out.println("Cargando reporte compilado...");
            // Cargar el reporte compilado
            JasperReport report = (JasperReport) JRLoader.loadObject(reportStream);
            System.out.println("✓ Reporte compilado cargado exitosamente");

            // Crear el datasource con los datos de los juegos
            JRBeanArrayDataSource dataSource = new JRBeanArrayDataSource(gamesList.toArray());

            // Preparar los parámetros del reporte
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("ds", dataSource);

            System.out.println("Llenando reporte con datos...");
            System.out.println("Total registros a mostrar: " + gamesList.size());
            
            // Verificar que los datos se convirtieron correctamente
            if (gamesList.size() > 1) {
                TopGameData firstGame = gamesList.get(1);
                System.out.println("Verificando primer registro de juego:");
                System.out.println("  - name: " + firstGame.getName());
                System.out.println("  - companyName: " + firstGame.getCompanyName());
                System.out.println("  - price: " + firstGame.getPrice());
                System.out.println("  - totalSales: " + firstGame.getTotalSales());
                System.out.println("  - avgRating: " + firstGame.getAvgRating());
                System.out.println("  - ratingCount: " + firstGame.getRatingCount());
            }
            
            // Llenar el reporte: pasar el datasource en ambos lugares
            JasperPrint jasperPrint = JasperFillManager.fillReport(report, parameters, dataSource);
            System.out.println("✓ Reporte lleno con éxito");
            System.out.println("Número de páginas generadas: " + jasperPrint.getPages().size());

            // Configurar la respuesta HTTP para devolver un PDF
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "inline; filename=JuegosMasVendidos.pdf");

            System.out.println("Exportando a PDF...");
            // Exportar el reporte a PDF
            JasperExportManager.exportReportToPdfStream(jasperPrint, out);
            
            out.flush();
            System.out.println("✓ PDF generado exitosamente");

        } catch (SQLException ex) {
            Logger.getLogger(TopGamesReportExportController.class.getName())
                    .log(Level.SEVERE, "Error SQL al obtener datos para el reporte", ex);
            System.err.println("ERROR SQL: " + ex.getMessage());
            ex.printStackTrace();

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("text/plain;charset=UTF-8");
            
            try {
                out.println("Error SQL al generar el reporte: " + ex.getMessage());
                out.flush();
            } catch (Exception e) {
                Logger.getLogger(TopGamesReportExportController.class.getName())
                        .log(Level.SEVERE, "Error escribiendo respuesta de error", e);
            }

        } catch (Exception ex) {
            Logger.getLogger(TopGamesReportExportController.class.getName())
                    .log(Level.SEVERE, "Error inesperado al generar reporte PDF", ex);
            System.err.println("ERROR: " + ex.getMessage());
            ex.printStackTrace();

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("text/plain;charset=UTF-8");
            
            try {
                out.println("Ocurrió un error al intentar generar el reporte: " + ex.getMessage());
                out.flush();
            } catch (Exception e) {
                Logger.getLogger(TopGamesReportExportController.class.getName())
                        .log(Level.SEVERE, "Error escribiendo respuesta de error", e);
            }
        }
    }
}
