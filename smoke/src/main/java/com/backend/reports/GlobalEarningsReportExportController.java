package com.backend.reports;

import com.backend.daos.PurcharseDao;
import com.backend.entities.CompanyEarningsData;
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
 * Servlet que exporta el reporte de ganancias globales a PDF usando JasperReports.
 */
@WebServlet(name = "GlobalEarningsReportExportController", urlPatterns = {"/reports/global-earnings/export"})
public class GlobalEarningsReportExportController extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        ServletOutputStream out = response.getOutputStream();

        try {
            System.out.println("=== Iniciando generación de reporte de ganancias globales ===");
            
            // Intentar cargar el archivo .jasper desde diferentes rutas
            InputStream reportStream = this.getServletConfig()
                    .getServletContext()
                    .getResourceAsStream("/reports/SystemGlobalEarning.jasper");
            
            if (reportStream == null) {
                reportStream = this.getServletConfig()
                        .getServletContext()
                        .getResourceAsStream("/WEB-INF/classes/reports/SystemGlobalEarning.jasper");
            }

            if (reportStream == null) {
                System.err.println("ERROR: No se encontró el archivo .jasper");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.setContentType("text/plain;charset=UTF-8");
                out.println("Error: No se encontró el archivo de reporte SystemGlobalEarning.jasper");
                out.println("");
                out.println("Ubicaciones intentadas:");
                out.println("1. /reports/SystemGlobalEarning.jasper");
                out.println("2. /WEB-INF/classes/reports/SystemGlobalEarning.jasper");
                out.flush();
                return;
            }
            
            System.out.println("✓ Archivo .jasper encontrado");

            // Obtener datos de la base de datos
            System.out.println("Obteniendo datos de la base de datos...");
            PurcharseDao purchaseDao = new PurcharseDao("purcharse", "purcharse_id");
            JsonObject earningsData = purchaseDao.getGlobalEarningsStats();

            // Extraer totales
            Double totalRevenue = earningsData.has("totalRevenue") 
                    ? earningsData.get("totalRevenue").getAsDouble() : 0.0;
            Double totalCompanyEarnings = earningsData.has("totalCompanyEarnings") 
                    ? earningsData.get("totalCompanyEarnings").getAsDouble() : 0.0;
            Double totalCommissionRetained = earningsData.has("totalCommissionRetained") 
                    ? earningsData.get("totalCommissionRetained").getAsDouble() : 0.0;

            System.out.println("Total Revenue: " + totalRevenue);
            System.out.println("Total Company Earnings: " + totalCompanyEarnings);
            System.out.println("Total Commission Retained: " + totalCommissionRetained);

            // Convertir el desglose de empresas a objetos Java
            JsonArray companiesArray = earningsData.has("companiesBreakdown") 
                    ? earningsData.getAsJsonArray("companiesBreakdown") : new JsonArray();

            List<CompanyEarningsData> companiesList = new ArrayList<>();
            System.out.println("Procesando " + companiesArray.size() + " empresas...");
            
            // IMPORTANTE: Agregar un registro vacío al inicio (estructura del ejemplo servlet)
            companiesList.add(new CompanyEarningsData());
            
            for (int i = 0; i < companiesArray.size(); i++) {
                JsonObject companyJson = companiesArray.get(i).getAsJsonObject();
                CompanyEarningsData company = new CompanyEarningsData();
                
                company.setCompanyName(companyJson.get("companyName").getAsString());
                company.setSalesCount(companyJson.get("salesCount").getAsInt());
                company.setTotalSales(companyJson.get("totalSales").getAsDouble());
                company.setCommissionPercentage(companyJson.get("commissionPercentage").getAsDouble());
                company.setCommissionRetained(companyJson.get("commissionRetained").getAsDouble());
                company.setCompanyEarnings(companyJson.get("companyEarnings").getAsDouble());
                
                companiesList.add(company);
                System.out.println("  - " + company.getCompanyName() + ": " + company.getCompanyEarnings());
            }

            // Si no hay datos, el registro vacío anterior ya está
            System.out.println("Total registros con vacío inicial: " + companiesList.size());

            System.out.println("Cargando reporte compilado...");
            // Cargar el reporte compilado
            JasperReport report = (JasperReport) JRLoader.loadObject(reportStream);
            System.out.println("✓ Reporte compilado cargado exitosamente");

            // Crear el datasource con los datos de las empresas
            JRBeanArrayDataSource dataSource = new JRBeanArrayDataSource(companiesList.toArray());

            // Preparar los parámetros del reporte
            Map<String, Object> parameters = new HashMap<>();
            // El reporte SystemGlobalEarning usa "ds" como parámetro para el datasource de la tabla
            parameters.put("ds", dataSource);

            System.out.println("Llenando reporte con datos...");
            System.out.println("Total registros a mostrar: " + companiesList.size());
            
            // Verificar que los datos se convirtieron correctamente
            if (!companiesList.isEmpty()) {
                CompanyEarningsData firstCompany = companiesList.get(0);
                System.out.println("Verificando primer registro:");
                System.out.println("  - companyName: " + firstCompany.getCompanyName());
                System.out.println("  - salesCount: " + firstCompany.getSalesCount());
                System.out.println("  - totalSales: " + firstCompany.getTotalSales());
                System.out.println("  - commissionPercentage: " + firstCompany.getCommissionPercentage());
                System.out.println("  - commissionRetained: " + firstCompany.getCommissionRetained());
                System.out.println("  - companyEarnings: " + firstCompany.getCompanyEarnings());
            }
            
            // Llenar el reporte: pasar el datasource en AMBOS lugares
            // - Como parámetro "ds" para que la tabla acceda a $P{ds}
            // - Como tercer argumento para evitar el warning de Connection nula
            JasperPrint jasperPrint = JasperFillManager.fillReport(report, parameters, dataSource);
            System.out.println("✓ Reporte lleno con éxito");
            System.out.println("Número de páginas generadas: " + jasperPrint.getPages().size());

            // Configurar la respuesta HTTP para devolver un PDF
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "inline; filename=GananciasGlobales.pdf");

            System.out.println("Exportando a PDF...");
            // Exportar el reporte a PDF
            JasperExportManager.exportReportToPdfStream(jasperPrint, out);
            
            out.flush();
            System.out.println("✓ PDF generado exitosamente");

        } catch (SQLException ex) {
            Logger.getLogger(GlobalEarningsReportExportController.class.getName())
                    .log(Level.SEVERE, "Error SQL al obtener datos para el reporte", ex);
            System.err.println("ERROR SQL: " + ex.getMessage());
            ex.printStackTrace();

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("text/plain;charset=UTF-8");
            
            try {
                out.println("Error SQL al generar el reporte: " + ex.getMessage());
                out.flush();
            } catch (Exception e) {
                Logger.getLogger(GlobalEarningsReportExportController.class.getName())
                        .log(Level.SEVERE, "Error escribiendo respuesta de error", e);
            }

        } catch (Exception ex) {
            Logger.getLogger(GlobalEarningsReportExportController.class.getName())
                    .log(Level.SEVERE, "Error inesperado al generar reporte PDF", ex);
            System.err.println("ERROR: " + ex.getMessage());
            ex.printStackTrace();

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("text/plain;charset=UTF-8");
            
            try {
                out.println("Ocurrió un error al intentar generar el reporte: " + ex.getMessage());
                out.flush();
            } catch (Exception e) {
                Logger.getLogger(GlobalEarningsReportExportController.class.getName())
                        .log(Level.SEVERE, "Error escribiendo respuesta de error", e);
            }
        }
    }
}
