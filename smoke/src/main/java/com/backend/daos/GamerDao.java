/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backend.daos;

import com.backend.crud.Crud;
import com.backend.entities.Gamer;
import com.backend.entities.User;
import com.backend.exceptions.AlreadyExistException;
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
        String sqlGamer = "INSERT INTO "+tabla+"(user_id, wallet, country, phone) VALUES (?,?,?,?)";
        
        PreparedStatement stmtGamer = CONNECTION.prepareStatement(sqlGamer);
        
        if (readByPk(entidad.getMail()) != null) {
            throw new AlreadyExistException();
        }
        
        UserDao user = new UserDao("user", "mail");
        user.create(entidad);
        
        
        stmtGamer.setString(1, entidad.getMail());
        stmtGamer.setDouble(2, entidad.getWallet());
        stmtGamer.setInt(3, entidad.getPhone());
        
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
        
        UserDao dao = new UserDao("user", "mail");
        User user = dao.readByPk(gamer.getMail());
        
        gamer.setNickname(user.getNickname());
        gamer.setPassword(user.getPassword());
        gamer.setBirthdate(user.getBirthdate());
        gamer.setType(user.getType());
        
        return gamer;
    }
    
    
    
}
