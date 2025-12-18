/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backend.daos;

import com.backend.crud.Crud;
import com.backend.entities.User;
import com.backend.entities.UserCompany;
import com.backend.exceptions.AlreadyExistException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author antho
 */
public class UserCompanyDao extends Crud<UserCompany> {

    public UserCompanyDao(String tabla, String codigo) {
        super(tabla, codigo);
    }

    @Override
    public void create(UserCompany entidad) throws SQLException, AlreadyExistException {
        String sql = "INSERT INTO "+tabla+"(user_id, company_id) VALUES (?)";
        
        PreparedStatement stmt = CONNECTION.prepareStatement(sql);
        
        UserDao user = new UserDao("user", "mail");
        user.create(entidad);
        
        if (readByPk(entidad.getMail()) != null) {
            throw new AlreadyExistException();
        }
        
        stmt.setString(1, entidad.getMail());
        stmt.setInt(2, entidad.getCompany_id());
        
        stmt.executeUpdate();
    }

    @Override
    public UserCompany obtenerEntidad(ResultSet rs) throws SQLException {
        UserCompany userCompany = new UserCompany();
        
        //Parte de userCompany
        userCompany.setMail(rs.getString("user_id"));
        userCompany.setCompany_id(rs.getInt("company_id"));
        //Parte de user
        UserDao dao = new UserDao("user", "mail");
        User user = dao.readByPk(userCompany.getMail());
        
        userCompany.setNickname(user.getNickname());
        userCompany.setPassword(user.getPassword());
        userCompany.setBirthdate(user.getBirthdate());
        userCompany.setType(user.getType());
        
        return userCompany;
    }
    
}
