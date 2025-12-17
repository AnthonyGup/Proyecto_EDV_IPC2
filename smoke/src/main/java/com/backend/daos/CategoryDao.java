/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backend.daos;

import com.backend.crud.Crud;
import com.backend.entities.Category;
import com.backend.exceptions.AlreadyExistException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author antho
 */
public class CategoryDao extends Crud<Category> {

    public CategoryDao(String tabla, String codigo) {
        super(tabla, codigo);
    }

    @Override
    public void create(Category entidad) throws SQLException, AlreadyExistException {
        String sql = "INSERT INTO "+tabla+" (name) VALUES (?)";
        PreparedStatement stmt = CONNECTION.prepareStatement(sql);
        if (readByColumn(entidad.getName(), "name") == null) {
            stmt.setString(1, entidad.getName());
            stmt.executeUpdate();
        } else {
            throw new AlreadyExistException();
        }        
    }

    @Override
    public Category obtenerEntidad(ResultSet rs) throws SQLException {
        Category category = new Category();
        
        category.setName(rs.getString("name"));
        category.setCategoryId(rs.getInt("category_id"));
        
        return category;
    }

    
}
