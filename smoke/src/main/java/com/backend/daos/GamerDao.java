/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backend.daos;

import com.backend.crud.Crud;
import com.backend.entities.Gamer;
import com.backend.entities.enums.UserType;
import com.backend.exceptions.AlreadyExistException;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author antho
 */
public class GamerDao extends Crud<Gamer> {

    public GamerDao(String tabla, String codigo) {
        super(tabla, codigo);
    }

    @Override
    public void create(Gamer entidad) throws SQLException, AlreadyExistException {
        String sqlUser = "INSERT INTO user (nickname, password, mail, birthdate, type) VALUES (?,?,?,?,?)";
        String sqlGamer = "INSERT INTO "+tabla+"(user_id, wallet, country, phone) VALUES (?,?,?,?)";
        
        PreparedStatement stmtUser = CONNECTION.prepareCall(sqlUser);
        PreparedStatement stmtGamer = CONNECTION.prepareCall(sqlGamer);
        
        if (readByPk(entidad.getMail()) != null) {
            throw new AlreadyExistException();
        }
        
        stmtUser.setString(1, entidad.getNickname());
        stmtUser.setString(2, entidad.getPassword());
        stmtUser.setString(3, entidad.getMail());
        stmtUser.setDate(4, Date.valueOf(entidad.getBirthdate()));
        stmtUser.setString(5, entidad.getType().name());
        
        stmtGamer.setString(1, entidad.getMail());
        stmtGamer.setDouble(2, entidad.getWallet());
        stmtGamer.setInt(3, entidad.getPhone());
        
        stmtUser.executeUpdate();
        stmtGamer.executeUpdate();
    }

    @Override
    public Gamer obtenerEntidad(ResultSet rs) throws SQLException {
        Gamer gamer = new Gamer();
        
        //Parte de gamer
        gamer.setMail(rs.getString("user_id"));
        gamer.setWallet(rs.getDouble("wallet"));
        gamer.setCountry(rs.getString("country"));
        gamer.setPhone(rs.getInt("phone"));
        //Parte de user
        
        String sql = "SELECT * FROM user WHERE mail = ?";
        PreparedStatement stmt = CONNECTION.prepareStatement(sql);
        stmt.setString(1, gamer.getMail());
        rs = stmt.executeQuery();
        
        gamer.setNickname(rs.getString("nickname"));
        gamer.setPassword(rs.getString("password"));
        gamer.setBirthdate(rs.getDate("birthdate").toLocalDate());
        gamer.setType(UserType.valueOf(rs.getString("type")));
        
        return gamer;
    }
    
    
    
}
