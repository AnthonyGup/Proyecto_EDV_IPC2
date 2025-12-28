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
    
    
}
