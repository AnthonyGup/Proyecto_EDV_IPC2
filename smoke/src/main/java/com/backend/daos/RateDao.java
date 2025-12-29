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
    
}
