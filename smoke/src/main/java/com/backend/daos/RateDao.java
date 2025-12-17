/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backend.daos;

import com.backend.crud.Crud;
import com.backend.entities.Rate;
import com.backend.exceptions.AlreadyExistException;
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
        String sql = "INSERT INTO "+tabla+" (user_id, game_id, stars) VALUES (?,?,?)";
        PreparedStatement stmt = CONNECTION.prepareStatement(sql);
        if (readByColumn(entidad.getName(), "name") == null) {
            stmt.setString(1, entidad.getName());
            stmt.executeUpdate();
        } else {
            throw new AlreadyExistException();
        }   
    }

    @Override
    public Rate obtenerEntidad(ResultSet rs) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
    
}
