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
        String sql = "INSERT INTO "+tabla+"(videogame_id, relase_data, min_requirements, price, description, name, company_id, age_restriction, available) VALUES (?,?,?,?,?,?,?,?,?)";
        PreparedStatement stmt = CONNECTION.prepareStatement(sql);
        
        if (readByPk("" + entidad.getVideogameId()) != null) {
            throw new AlreadyExistException();
        }
        
        stmt.setInt(1, entidad.getVideogameId());
        stmt.setDate(2, Date.valueOf(entidad.getRelasedate()));
        stmt.setString(3, entidad.getMinimRequirements());
        stmt.setDouble(4, entidad.getPrice());
        stmt.setString(5, entidad.getDescription());
        stmt.setString(6, entidad.getName());
        stmt.setInt(7, entidad.getCompanyId());
        stmt.setInt(8, entidad.getAgeRestriction());
        stmt.setBoolean(9, entidad.isAvailable());
        
        stmt.executeUpdate();
    }   

    @Override
    public Videogame obtenerEntidad(ResultSet rs) throws SQLException {
        Videogame game = new Videogame();
        
        game.setVideogameId(rs.getInt("videogame_id"));
        game.setRelasedate(rs.getDate("realse_date").toLocalDate());
        game.setMinimRequirements(rs.getString("minim_requirements"));
        game.setPrice(rs.getDouble("price"));
        game.setDescription(rs.getString("description"));
        game.setName(rs.getString("name"));
        game.setCompanyId(rs.getInt("company_id"));
        game.setAgeRestriction(rs.getInt("age_restriction"));
        game.setAvailable(rs.getBoolean("available"));
        
        return game;
    }
    
    
}
