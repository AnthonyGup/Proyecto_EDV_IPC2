/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backend.daos;

import com.backend.crud.Crud;
import com.backend.entities.User;
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
public class UserDao extends Crud<User> {

    public UserDao(String tabla, String codigo) {
        super(tabla, codigo);
    }

    @Override
    public void create(User entidad) throws SQLException, AlreadyExistException {
        String sqlUser = "INSERT INTO user (nickname, password, mail, birthdate, type) VALUES (?,?,?,?,?)";
        PreparedStatement stmtUser = CONNECTION.prepareCall(sqlUser);
        
        if (readByPk(entidad.getMail()) != null) {
            throw new AlreadyExistException();
        }
        
        stmtUser.setString(1, entidad.getNickname());
        stmtUser.setString(2, entidad.getPassword());
        stmtUser.setString(3, entidad.getMail());
        stmtUser.setDate(4, Date.valueOf(entidad.getBirthdate()));
        stmtUser.setString(5, entidad.getType().name());
        
        stmtUser.executeUpdate();
    }

    @Override
    public User obtenerEntidad(ResultSet rs) throws SQLException {
        User user = new User();
        
        user.setMail(rs.getString("user_id"));
        user.setNickname(rs.getString("nickname"));
        user.setPassword(rs.getString("password"));
        user.setBirthdate(rs.getDate("birthdate").toLocalDate());
        user.setType(UserType.valueOf(rs.getString("type")));
        
        return user;
    }
    
    
}
