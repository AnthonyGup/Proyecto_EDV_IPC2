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
    
}
