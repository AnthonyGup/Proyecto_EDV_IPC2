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
        String sql = "INSERT INTO"+tabla+"(category_id, game_id, videogameCategory_id) VALUES (?,?,?)";
        
        PreparedStatement stmt = CONNECTION.prepareStatement(sql);
        
        int id = entidad.getGameId() + entidad.getCategoryId();
        
        if (readByPk("" + id) != null) {
            throw new AlreadyExistException();
        }
        
        stmt.setInt(1, entidad.getCategoryId());
        stmt.setInt(2, entidad.getGameId());
        stmt.setInt(3, id);
    }

    @Override
    public VideogameCategory obtenerEntidad(ResultSet rs) throws SQLException {
        VideogameCategory game =  new VideogameCategory();
        
        game.setCategoryId(rs.getInt("category_id"));
        game.setGameId(rs.getInt("game_id"));
        game.setVcId(rs.getInt("videogameCategory_id"));
        
        return game;
    }
    
    
}
