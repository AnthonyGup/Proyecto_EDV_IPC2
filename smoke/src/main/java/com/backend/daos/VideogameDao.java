/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backend.daos;

import com.backend.crud.Crud;
import com.backend.entities.Videogame;
import com.backend.exceptions.AlreadyExistException;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author antho
 */
public class VideogameDao extends Crud<Videogame> {

    public VideogameDao(String tabla, String codigo) {
        super(tabla, codigo);
    }

    @Override
    public void create(Videogame entidad) throws SQLException, AlreadyExistException {
        String sql = "INSERT INTO "+tabla+"(name, description, release_date, min_requirements, price, available, age_restriction, company_id) VALUES (?,?,?,?,?,?,?,?)";
        PreparedStatement stmt = CONNECTION.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
        
        stmt.setString(1, entidad.getName());
        stmt.setString(2, entidad.getDescription());
        stmt.setDate(3, Date.valueOf(entidad.getRelasedate()));
        stmt.setString(4, entidad.getMinimRequirements());
        stmt.setDouble(5, entidad.getPrice());
        stmt.setBoolean(6, entidad.isAvailable());
        stmt.setInt(7, entidad.getAgeRestriction());
        stmt.setInt(8, entidad.getCompanyId());
        
        stmt.executeUpdate();
        
            // Obtener el ID generado automáticamente
            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                entidad.setVideogameId(generatedKeys.getInt(1));
            }
    }   

    @Override
    public Videogame obtenerEntidad(ResultSet rs) throws SQLException {
        Videogame game = new Videogame();
        
        game.setVideogameId(rs.getInt("videogame_id"));
        game.setRelasedate(rs.getDate("release_date").toLocalDate());
        game.setMinimRequirements(rs.getString("min_requirements"));
        game.setPrice(rs.getDouble("price"));
        game.setDescription(rs.getString("description"));
        game.setName(rs.getString("name"));
        game.setCompanyId(rs.getInt("company_id"));
        game.setAgeRestriction(rs.getInt("age_restriction"));
        game.setAvailable(rs.getBoolean("available"));
        
        return game;
    }

    /**
     * Obtiene el top de juegos por ventas y calidad.
     * Permite filtrar por categoría o edad.
     * 
     * @param sortBy "sales" para ordenar por ventas, "rating" para ordenar por calificación
     * @param categoryId ID de categoría (null para incluir todas)
     * @param ageRestriction Restricción de edad (null para incluir todas)
     * @param limit Número máximo de resultados
     * @return JsonArray con los juegos top ordenados
     */
    public com.google.gson.JsonArray getTopGamesByQualityAndSales(String sortBy, Integer categoryId, Integer ageRestriction, int limit) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT vg.videogame_id, vg.name, vg.description, vg.price, vg.age_restriction, " +
            "       vg.company_id, c.name as company_name, " +
            "       COALESCE(COUNT(DISTINCT p.purcharse_id), 0) as total_sales, " +
            "       COALESCE(AVG(r.stars), 0) as avg_rating, " +
            "       COALESCE(COUNT(DISTINCT CONCAT(r.user_id, '-', r.game_id)), 0) as rating_count " +
            "FROM videogame vg " +
            "LEFT JOIN purcharse p ON vg.videogame_id = p.game_id " +
            "LEFT JOIN rate r ON vg.videogame_id = r.game_id " +
            "LEFT JOIN company c ON vg.company_id = c.company_id " +
            "LEFT JOIN videogameCategory vc ON vg.videogame_id = vc.game_id "
        );

        // Agregar filtros
        boolean hasWhere = false;
        if (categoryId != null) {
            sql.append("WHERE vc.category_id = ? ");
            hasWhere = true;
        }
        if (ageRestriction != null) {
            sql.append(hasWhere ? "AND " : "WHERE ");
            sql.append("vg.age_restriction <= ? ");
            hasWhere = true;
        }

        // Agrupar
        sql.append("GROUP BY vg.videogame_id, vg.name, vg.description, vg.price, vg.age_restriction, vg.company_id, c.name ");

        // Ordenar
        if ("rating".equalsIgnoreCase(sortBy)) {
            sql.append("ORDER BY avg_rating DESC, total_sales DESC ");
        } else {
            sql.append("ORDER BY total_sales DESC, avg_rating DESC ");
        }

        sql.append("LIMIT ?");

        PreparedStatement stmt = CONNECTION.prepareStatement(sql.toString());
        int paramIndex = 1;

        if (categoryId != null) {
            stmt.setInt(paramIndex++, categoryId);
        }
        if (ageRestriction != null) {
            stmt.setInt(paramIndex++, ageRestriction);
        }
        stmt.setInt(paramIndex, limit);

        ResultSet rs = stmt.executeQuery();
        com.google.gson.JsonArray gamesArray = new com.google.gson.JsonArray();

        while (rs.next()) {
            com.google.gson.JsonObject game = new com.google.gson.JsonObject();
            game.addProperty("videogameId", rs.getInt("videogame_id"));
            game.addProperty("name", rs.getString("name"));
            game.addProperty("description", rs.getString("description"));
            game.addProperty("price", rs.getDouble("price"));
            game.addProperty("ageRestriction", rs.getInt("age_restriction"));
            game.addProperty("companyId", rs.getInt("company_id"));
            game.addProperty("companyName", rs.getString("company_name"));
            game.addProperty("totalSales", rs.getInt("total_sales"));
            game.addProperty("avgRating", rs.getDouble("avg_rating"));
            game.addProperty("ratingCount", rs.getInt("rating_count"));

            gamesArray.add(game);
        }

        return gamesArray;
    }

    /**
     * Obtiene los juegos comprados por un usuario, con su favorabilidad.
     */
    public com.google.gson.JsonArray getGamerLibraryAnalysis(String userId) throws SQLException {
        String sql = "SELECT vg.videogame_id, vg.name, vc.category_id, cat.name as category_name, " +
                "p.date as purchase_date, " +
                "COALESCE(r.stars, 0) as personal_rating, " +
                "COALESCE(AVG(r2.stars), 0) as community_rating, " +
                "COALESCE(COUNT(DISTINCT r2.user_id), 0) as rating_count " +
                "FROM purcharse p " +
                "JOIN videogame vg ON p.game_id = vg.videogame_id " +
                "LEFT JOIN videogameCategory vc ON vg.videogame_id = vc.game_id " +
                "LEFT JOIN category cat ON vc.category_id = cat.category_id " +
                "LEFT JOIN rate r ON p.user_id = r.user_id AND vg.videogame_id = r.game_id " +
                "LEFT JOIN rate r2 ON vg.videogame_id = r2.game_id " +
                "WHERE p.user_id = ? " +
                "GROUP BY vg.videogame_id, vg.name, vc.category_id, cat.name, p.date, r.stars " +
                "ORDER BY p.date DESC";
        
        PreparedStatement stmt = CONNECTION.prepareStatement(sql);
        stmt.setString(1, userId);
        ResultSet rs = stmt.executeQuery();
        
        com.google.gson.JsonArray arr = new com.google.gson.JsonArray();
        while (rs.next()) {
            com.google.gson.JsonObject obj = new com.google.gson.JsonObject();
            obj.addProperty("gameId", rs.getInt("videogame_id"));
            obj.addProperty("gameTitle", rs.getString("name"));
            obj.addProperty("categoryId", rs.getInt("category_id"));
            obj.addProperty("categoryName", rs.getString("category_name"));
            obj.addProperty("purchaseDate", rs.getDate("purchase_date").toString());
            obj.addProperty("personalRating", rs.getInt("personal_rating"));
            obj.addProperty("communityRating", rs.getDouble("community_rating"));
            obj.addProperty("ratingCount", rs.getInt("rating_count"));
            arr.add(obj);
        }
        return arr;
    }

    /**
     * Obtiene categorías favoritas del usuario por cantidad de juegos comprados.
     */
    public com.google.gson.JsonArray getGamerFavoriteCategories(String userId) throws SQLException {
        String sql = "SELECT cat.category_id, cat.name, COUNT(DISTINCT p.game_id) as game_count " +
                "FROM purcharse p " +
                "JOIN videogame vg ON p.game_id = vg.videogame_id " +
                "JOIN videogameCategory vc ON vg.videogame_id = vc.game_id " +
                "JOIN category cat ON vc.category_id = cat.category_id " +
                "WHERE p.user_id = ? " +
                "GROUP BY cat.category_id, cat.name " +
                "ORDER BY game_count DESC";
        
        PreparedStatement stmt = CONNECTION.prepareStatement(sql);
        stmt.setString(1, userId);
        ResultSet rs = stmt.executeQuery();
        
        com.google.gson.JsonArray arr = new com.google.gson.JsonArray();
        while (rs.next()) {
            com.google.gson.JsonObject obj = new com.google.gson.JsonObject();
            obj.addProperty("categoryId", rs.getInt("category_id"));
            obj.addProperty("categoryName", rs.getString("name"));
            obj.addProperty("gameCount", rs.getInt("game_count"));
            arr.add(obj);
        }
        return arr;
    }
    
}
