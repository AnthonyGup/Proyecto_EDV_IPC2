/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backend.daos;

import com.backend.crud.Crud;
import com.backend.entities.Purcharse;
import com.backend.exceptions.AlreadyExistException;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author antho
 */
public class PurcharseDao extends Crud<Purcharse>  {

    public PurcharseDao(String tabla, String codigo) {
        super(tabla, codigo);
    }

    @Override
    public void create(Purcharse entidad) throws SQLException, AlreadyExistException {
        // Evitar compras duplicadas (usuario + juego)
        if (existsByUserAndGame(entidad.getUserId(), entidad.getGameId())) {
            throw new AlreadyExistException();
        }

        String sql = "INSERT INTO " + tabla + " (game_id, price, date, user_id) VALUES (?,?,?,?)";

        PreparedStatement stmt = CONNECTION.prepareStatement(sql);

        stmt.setInt(1, entidad.getGameId());
        stmt.setDouble(2, entidad.getPrice());
        stmt.setDate(3, Date.valueOf(entidad.getDate()));
        stmt.setString(4, entidad.getUserId());

        stmt.executeUpdate();
    }

    @Override
    public Purcharse obtenerEntidad(ResultSet rs) throws SQLException {
        Purcharse buy = new Purcharse();
        
        buy.setUserId(rs.getString("user_id"));
        buy.setDate(rs.getDate("date").toLocalDate());
        buy.setPrice(rs.getDouble("price"));
        buy.setPurcharseId(rs.getInt("purcharse_id"));
        buy.setGameId(rs.getInt("game_id"));
        
        return buy;
    }

    /**
     * Verifica si ya existe una compra para el usuario y el juego.
     */
    public boolean existsByUserAndGame(String userId, int gameId) throws SQLException {
        String sql = "SELECT 1 FROM " + tabla + " WHERE user_id = ? AND game_id = ? LIMIT 1";
        PreparedStatement stmt = CONNECTION.prepareStatement(sql);
        stmt.setString(1, userId);
        stmt.setInt(2, gameId);
        ResultSet rs = stmt.executeQuery();
        return rs.next();
    }

    public void deleteByUserAndGame(String userId, int gameId) throws SQLException {
        String sql = "DELETE FROM " + tabla + " WHERE user_id = ? AND game_id = ?";
        PreparedStatement stmt = CONNECTION.prepareStatement(sql);
        stmt.setString(1, userId);
        stmt.setInt(2, gameId);
        stmt.executeUpdate();
    }

    /**
     * Obtiene estadísticas de ventas globales del sistema:
     * - Total de dinero ingresado al sistema
     * - Desglose por empresa (ingresos brutos)
     * - Comisión global retenida
     * - Dinero neto para empresas
     */
    public com.google.gson.JsonObject getGlobalEarningsStats() throws SQLException {
        com.google.gson.JsonObject stats = new com.google.gson.JsonObject();
        
        // Total de todas las ventas
        String sqlTotal = "SELECT SUM(price) AS total_revenue FROM " + tabla;
        PreparedStatement stmt = CONNECTION.prepareStatement(sqlTotal);
        ResultSet rs = stmt.executeQuery();
        
        double totalRevenue = 0;
        if (rs.next()) {
            totalRevenue = rs.getDouble("total_revenue");
        }
        
        // Estadísticas por empresa (juegos vendidos por empresa y comisión)
        String sqlByCompany = "SELECT vg.company_id, c.name, COUNT(p.purcharse_id) as sales_count, " +
                "SUM(p.price) as total_sales, c.commission " +
                "FROM purcharse p " +
                "JOIN videogame vg ON p.game_id = vg.videogame_id " +
                "JOIN company c ON vg.company_id = c.company_id " +
                "GROUP BY vg.company_id, c.name, c.commission";
        
        stmt = CONNECTION.prepareStatement(sqlByCompany);
        rs = stmt.executeQuery();
        
        com.google.gson.JsonArray companiesArray = new com.google.gson.JsonArray();
        double totalCompanyEarnings = 0;
        double totalCommissionRetained = 0;
        
        while (rs.next()) {
            com.google.gson.JsonObject company = new com.google.gson.JsonObject();
            int companyId = rs.getInt("company_id");
            String companyName = rs.getString("name");
            int salesCount = rs.getInt("sales_count");
            double totalSales = rs.getDouble("total_sales");
            double companyCommission = rs.getDouble("commission");
            
            double companyEarnings = totalSales * (1 - companyCommission);
            double commissionRetained = totalSales * companyCommission;
            
            company.addProperty("companyId", companyId);
            company.addProperty("companyName", companyName);
            company.addProperty("salesCount", salesCount);
            company.addProperty("totalSales", totalSales);
            company.addProperty("commissionPercentage", companyCommission * 100);
            company.addProperty("companyEarnings", companyEarnings);
            company.addProperty("commissionRetained", commissionRetained);
            
            companiesArray.add(company);
            totalCompanyEarnings += companyEarnings;
            totalCommissionRetained += commissionRetained;
        }
        
        stats.addProperty("totalRevenue", totalRevenue);
        stats.addProperty("totalCompanyEarnings", totalCompanyEarnings);
        stats.addProperty("totalCommissionRetained", totalCommissionRetained);
        stats.add("companiesBreakdown", companiesArray);
        
        return stats;
    }

    /**
     * Top usuarios por compras (cantidad y gasto total).
     * @param limit máximo de usuarios a retornar
     */
    public com.google.gson.JsonArray getTopUsersByPurchases(int limit) throws SQLException {
        String sql = "SELECT p.user_id, COUNT(p.purcharse_id) AS purchases_count, "
                + "SUM(p.price) AS total_spent, COUNT(DISTINCT p.game_id) AS unique_games "
                + "FROM purcharse p "
                + "GROUP BY p.user_id "
                + "ORDER BY purchases_count DESC, total_spent DESC "
                + "LIMIT ?";

        PreparedStatement stmt = CONNECTION.prepareStatement(sql);
        stmt.setInt(1, limit);
        ResultSet rs = stmt.executeQuery();

        com.google.gson.JsonArray arr = new com.google.gson.JsonArray();
        while (rs.next()) {
            com.google.gson.JsonObject obj = new com.google.gson.JsonObject();
            obj.addProperty("userId", rs.getString("user_id"));
            obj.addProperty("purchasesCount", rs.getInt("purchases_count"));
            obj.addProperty("totalSpent", rs.getDouble("total_spent"));
            obj.addProperty("uniqueGames", rs.getInt("unique_games"));
            arr.add(obj);
        }
        return arr;
    }

    /**
     * Obtiene historial de compras de un usuario específico.
     */
    public com.google.gson.JsonArray getExpenseHistoryByUser(String userId) throws SQLException {
        String sql = "SELECT p.purcharse_id, p.game_id, vg.name, p.price, p.date " +
                "FROM purcharse p " +
                "JOIN videogame vg ON p.game_id = vg.videogame_id " +
                "WHERE p.user_id = ? " +
                "ORDER BY p.date DESC";
        
        PreparedStatement stmt = CONNECTION.prepareStatement(sql);
        stmt.setString(1, userId);
        ResultSet rs = stmt.executeQuery();
        
        com.google.gson.JsonArray arr = new com.google.gson.JsonArray();
        while (rs.next()) {
            com.google.gson.JsonObject obj = new com.google.gson.JsonObject();
            obj.addProperty("purcharseId", rs.getInt("purcharse_id"));
            obj.addProperty("gameId", rs.getInt("game_id"));
            obj.addProperty("gameTitle", rs.getString("name"));
            obj.addProperty("price", rs.getDouble("price"));
            obj.addProperty("date", rs.getDate("date").toString());
            arr.add(obj);
        }
        return arr;
    }

    /**
     * Obtiene ventas por juego para una empresa específica.
     */
    public com.google.gson.JsonArray getGameSalesByCompany(int companyId) throws SQLException {
        String sql = "SELECT vg.videogame_id, vg.name, COUNT(p.purcharse_id) AS sales_count, " +
                "SUM(p.price) AS total_sales, c.commission " +
                "FROM purcharse p " +
                "JOIN videogame vg ON p.game_id = vg.videogame_id " +
                "JOIN company c ON vg.company_id = c.company_id " +
                "WHERE vg.company_id = ? " +
                "GROUP BY vg.videogame_id, vg.name, c.commission " +
                "ORDER BY total_sales DESC";
        
        PreparedStatement stmt = CONNECTION.prepareStatement(sql);
        stmt.setInt(1, companyId);
        ResultSet rs = stmt.executeQuery();
        
        com.google.gson.JsonArray arr = new com.google.gson.JsonArray();
        while (rs.next()) {
            com.google.gson.JsonObject obj = new com.google.gson.JsonObject();
            double totalSales = rs.getDouble("total_sales");
            double commission = rs.getDouble("commission");
            double netEarnings = totalSales * (1 - commission);
            
            obj.addProperty("gameId", rs.getInt("videogame_id"));
            obj.addProperty("gameTitle", rs.getString("name"));
            obj.addProperty("salesCount", rs.getInt("sales_count"));
            obj.addProperty("totalSales", totalSales);
            obj.addProperty("commissionPercentage", commission * 100);
            obj.addProperty("netEarnings", netEarnings);
            arr.add(obj);
        }
        return arr;
    }

    /**
     * Top 5 juegos más vendidos de una empresa en un intervalo de tiempo.
     */
    public com.google.gson.JsonArray getTopGamesByCompanyInTimeRange(int companyId, String startDate, String endDate, int limit) throws SQLException {
        String sql = "SELECT vg.videogame_id, vg.name, COUNT(p.purcharse_id) AS sales_count, " +
                "SUM(p.price) AS total_sales " +
                "FROM purcharse p " +
                "JOIN videogame vg ON p.game_id = vg.videogame_id " +
                "WHERE vg.company_id = ? AND p.date BETWEEN ? AND ? " +
                "GROUP BY vg.videogame_id, vg.name " +
                "ORDER BY sales_count DESC " +
                "LIMIT ?";
        
        PreparedStatement stmt = CONNECTION.prepareStatement(sql);
        stmt.setInt(1, companyId);
        stmt.setString(2, startDate);
        stmt.setString(3, endDate);
        stmt.setInt(4, limit);
        ResultSet rs = stmt.executeQuery();
        
        com.google.gson.JsonArray arr = new com.google.gson.JsonArray();
        while (rs.next()) {
            com.google.gson.JsonObject obj = new com.google.gson.JsonObject();
            obj.addProperty("gameId", rs.getInt("videogame_id"));
            obj.addProperty("gameTitle", rs.getString("name"));
            obj.addProperty("salesCount", rs.getInt("sales_count"));
            obj.addProperty("totalSales", rs.getDouble("total_sales"));
            arr.add(obj);
        }
        return arr;
    }
    
}
