/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backend.daos;

import com.backend.crud.Crud;
import com.backend.entities.VideogameCategory;
import com.backend.exceptions.AlreadyExistException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author antho
 */
public class VideogameCategoryDao extends Crud<VideogameCategory> {

    public VideogameCategoryDao(String tabla, String codigo) {
        super(tabla, codigo);
    }

    @Override
    public void create(VideogameCategory entidad) throws SQLException, AlreadyExistException {
        String sql = "INSERT INTO " + tabla + "(category_id, game_id) VALUES (?,?)";
        
        PreparedStatement stmt = CONNECTION.prepareStatement(sql);
        
        stmt.setInt(1, entidad.getCategoryId());
        stmt.setInt(2, entidad.getGameId());
        
        stmt.executeUpdate();
        stmt.close();
    }

    @Override
    public VideogameCategory obtenerEntidad(ResultSet rs) throws SQLException {
        VideogameCategory game =  new VideogameCategory();
        
        game.setCategoryId(rs.getInt("category_id"));
        game.setGameId(rs.getInt("game_id"));
        
        return game;
    }
    
    public List<VideogameCategory> readByGameId(int gameId) throws SQLException {
        List<VideogameCategory> categories = new ArrayList<>();
        String sql = "SELECT * FROM " + tabla + " WHERE game_id = ?";
        
        PreparedStatement stmt = CONNECTION.prepareStatement(sql);
        stmt.setInt(1, gameId);
        ResultSet rs = stmt.executeQuery();
        
        while (rs.next()) {
            categories.add(obtenerEntidad(rs));
        }
        
        return categories;
    }
    
    public List<VideogameCategory> readByCategoryId(int categoryId) throws SQLException {
        List<VideogameCategory> games = new ArrayList<>();
        String sql = "SELECT * FROM " + tabla + " WHERE category_id = ?";
        
        PreparedStatement stmt = CONNECTION.prepareStatement(sql);
        stmt.setInt(1, categoryId);
        ResultSet rs = stmt.executeQuery();
        
        while (rs.next()) {
            games.add(obtenerEntidad(rs));
        }
        
        return games;
    }
    
    /**
     * Elimina todas las categorías asociadas a un videojuego
     * @param gameId ID del videojuego
     * @throws SQLException
     */
    public void deleteByGameId(int gameId) throws SQLException {
        String sql = "DELETE FROM " + tabla + " WHERE game_id = ?";
        PreparedStatement stmt = CONNECTION.prepareStatement(sql);
        stmt.setInt(1, gameId);
        stmt.executeUpdate();
        stmt.close();
    }
    
    /**
     * Inserta múltiples categorías para un videojuego
     * @param gameId ID del videojuego
     * @param categoryIds Lista de IDs de categorías a insertar
     * @throws SQLException
     */
    public void insertBulkCategories(int gameId, List<Integer> categoryIds) throws SQLException {
        String sql = "INSERT INTO " + tabla + "(game_id, category_id) VALUES (?, ?)";
        PreparedStatement stmt = CONNECTION.prepareStatement(sql);
        
        for (Integer categoryId : categoryIds) {
            stmt.setInt(1, gameId);
            stmt.setInt(2, categoryId);
            stmt.executeUpdate();
        }
        
        stmt.close();
    }
    
    /**
     * Actualiza todas las categorías de un videojuego
     * Elimina las categorías actuales e inserta las nuevas
     * @param gameId ID del videojuego
     * @param categoryIds Lista de IDs de categorías nuevas
     * @throws SQLException
     */
    public void updateGameCategories(int gameId, List<Integer> categoryIds) throws SQLException {
        deleteByGameId(gameId);
        insertBulkCategories(gameId, categoryIds);
    }
}
