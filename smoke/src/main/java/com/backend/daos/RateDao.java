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
        String sql = "INSERT INTO "+tabla+" (rate_id, user_id, game_id, stars) VALUES (?,?,?,?)";
        PreparedStatement stmt = CONNECTION.prepareStatement(sql);
        
        String id = entidad.getUserId() + entidad.getGameId();
        
        stmt.setString(1, id);
        stmt.setString(2, entidad.getUserId());
        stmt.setInt(3, entidad.getGameId());
        stmt.setInt(4, entidad.getStars());
        
        if (readByPk(id) != null) {
            throw new AlreadyExistException();
        }
        
        stmt.executeUpdate();
    }

    @Override
    public Rate obtenerEntidad(ResultSet rs) throws SQLException {
        Rate rate = new Rate();
        
        rate.setRateId(rs.getString("rate_id"));
        rate.setUserId(rs.getString("user_id"));
        rate.setGameId(rs.getInt("game_id"));
        rate.setStars(rs.getInt("stars"));
        
        return rate;
    }
    
    
}
