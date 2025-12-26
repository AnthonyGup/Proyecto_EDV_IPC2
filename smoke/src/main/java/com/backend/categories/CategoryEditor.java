/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backend.categories;

import com.backend.daos.CategoryDao;
import com.backend.entities.Category;
import com.backend.exceptions.AlreadyExistException;
import java.sql.SQLException;

/**
 *
 * @author antho
 */
public class CategoryEditor {
    
    public void editCategory(Category category, String nombreNuevo) throws SQLException, AlreadyExistException {
        CategoryDao dao = new CategoryDao("category", "category_id");
        
        if (dao.readByColumn(nombreNuevo, "name") == null) {
            throw new AlreadyExistException();
        }
        
        dao.update(category.getCategoryId() + "", "name", nombreNuevo);
    }
    
    //Create y delete se haceen en el servlet
}
