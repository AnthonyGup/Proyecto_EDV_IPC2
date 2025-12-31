/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backend.daos;

import com.backend.crud.Crud;
import com.backend.entities.Rate;
import com.backend.exceptions.AlreadyExistException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author antho
 */
public class RateDao extends Crud<Rate> {

    public RateDao(String tabla, String codigo) {
        super(tabla, codigo);
    }

    @Override
    public void create(Rate entidad) throws SQLException, AlreadyExistException {
        if (existsByUserAndGame(entidad.getUserId(), entidad.getGameId())) {
            throw new AlreadyExistException();
        }
        String sql = "INSERT INTO " + tabla + " (user_id, game_id, stars) VALUES (?,?,?)";
        PreparedStatement stmt = CONNECTION.prepareStatement(sql);
        stmt.setString(1, entidad.getUserId());
        stmt.setInt(2, entidad.getGameId());
        stmt.setInt(3, entidad.getStars());
        stmt.executeUpdate();
    }

    @Override
    public Rate obtenerEntidad(ResultSet rs) throws SQLException {
        Rate rate = new Rate();
        
        rate.setUserId(rs.getString("user_id"));
        rate.setGameId(rs.getInt("game_id"));
        rate.setStars(rs.getInt("stars"));
        
        return rate;
    }
    
    public boolean existsByUserAndGame(String userId, int gameId) throws SQLException {
        String sql = "SELECT 1 FROM " + tabla + " WHERE user_id = ? AND game_id = ? LIMIT 1";
        PreparedStatement stmt = CONNECTION.prepareStatement(sql);
        stmt.setString(1, userId);
        stmt.setInt(2, gameId);
        ResultSet rs = stmt.executeQuery();
        return rs.next();
    }

    /**
     * Obtiene calificaciones promedio de los juegos de una empresa.
     */
    public com.google.gson.JsonArray getAverageRatingByGameForCompany(int companyId) throws SQLException {
        String sql = "SELECT vg.videogame_id, vg.name, " +
                "AVG(r.stars) AS average_rating, COUNT(r.user_id) AS rating_count " +
                "FROM videogame vg " +
                "LEFT JOIN rate r ON vg.videogame_id = r.game_id " +
                "WHERE vg.company_id = ? " +
                "GROUP BY vg.videogame_id, vg.name " +
                "ORDER BY average_rating DESC";
        
        PreparedStatement stmt = CONNECTION.prepareStatement(sql);
        stmt.setInt(1, companyId);
        ResultSet rs = stmt.executeQuery();
        
        com.google.gson.JsonArray arr = new com.google.gson.JsonArray();
        while (rs.next()) {
            com.google.gson.JsonObject obj = new com.google.gson.JsonObject();
            obj.addProperty("gameId", rs.getInt("videogame_id"));
            obj.addProperty("gameTitle", rs.getString("name"));
            obj.addProperty("averageRating", rs.getDouble("average_rating"));
            obj.addProperty("ratingCount", rs.getInt("rating_count"));
            arr.add(obj);
        }
        return arr;
    }

    /**
     * Obtiene las peores calificaciones de los juegos de una empresa.
     */
    public com.google.gson.JsonArray getWorstRatedGamesByCompany(int companyId, int limit) throws SQLException {
        String sql = "SELECT vg.videogame_id, vg.name, AVG(r.stars) AS average_rating, COUNT(r.user_id) AS rating_count " +
                "FROM videogame vg " +
                "LEFT JOIN rate r ON vg.videogame_id = r.game_id " +
                "WHERE vg.company_id = ? " +
                "GROUP BY vg.videogame_id, vg.name " +
                "ORDER BY average_rating ASC, rating_count DESC " +
                "LIMIT ?";
        
        PreparedStatement stmt = CONNECTION.prepareStatement(sql);
        stmt.setInt(1, companyId);
        stmt.setInt(2, limit);
        ResultSet rs = stmt.executeQuery();
        
        com.google.gson.JsonArray arr = new com.google.gson.JsonArray();
        while (rs.next()) {
            com.google.gson.JsonObject obj = new com.google.gson.JsonObject();
            obj.addProperty("gameId", rs.getInt("videogame_id"));
            obj.addProperty("gameTitle", rs.getString("name"));
            obj.addProperty("averageRating", rs.getDouble("average_rating"));
            obj.addProperty("ratingCount", rs.getInt("rating_count"));
            arr.add(obj);
        }
        return arr;
    }

    /**
     * Obtiene comparativa de ratings personal vs comunidad para un usuario.
     */
    public com.google.gson.JsonArray getGamerRatesVsCommunitRates(String userId) throws SQLException {
        String sql = "SELECT vg.videogame_id, vg.name, " +
                "r.stars AS personal_rating, " +
                "AVG(r2.stars) AS community_rating, " +
                "COUNT(r2.user_id) AS rating_count " +
                "FROM rate r " +
                "JOIN videogame vg ON r.game_id = vg.videogame_id " +
                "LEFT JOIN rate r2 ON vg.videogame_id = r2.game_id " +
                "WHERE r.user_id = ? " +
                "GROUP BY vg.videogame_id, vg.name, r.stars " +
                "ORDER BY vg.name";
        
        PreparedStatement stmt = CONNECTION.prepareStatement(sql);
        stmt.setString(1, userId);
        ResultSet rs = stmt.executeQuery();
        
        com.google.gson.JsonArray arr = new com.google.gson.JsonArray();
        while (rs.next()) {
            com.google.gson.JsonObject obj = new com.google.gson.JsonObject();
            obj.addProperty("gameId", rs.getInt("videogame_id"));
            obj.addProperty("gameTitle", rs.getString("name"));
            obj.addProperty("personalRating", rs.getInt("personal_rating"));
            obj.addProperty("communityRating", rs.getDouble("community_rating"));
            obj.addProperty("ratingCount", rs.getInt("rating_count"));
            arr.add(obj);
        }
        return arr;
    }
    
}
