/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backend.categories;

import com.backend.daos.VideogameCategoryDao;
import com.backend.entities.Category;
import com.backend.entities.Videogame;
import com.backend.entities.VideogameCategory;
import com.backend.exceptions.AlreadyExistException;
import java.sql.SQLException;

/**
 *
 * @author antho
 */
public class CategorySetter {
    
    private VideogameCategoryDao dao = new VideogameCategoryDao("videogameCategory", "");;
    
    /**
     * Agrega una categoria a un videojuego
     * @param category
     * @param game 
     * @throws java.sql.SQLException 
     * @throws com.backend.exceptions.AlreadyExistException 
     */
    public void addVideogameCategory(Category category, Videogame game) throws SQLException, AlreadyExistException {
        
        VideogameCategory nueva = new VideogameCategory();
        nueva.setCategoryId(category.getCategoryId());
        nueva.setGameId(game.getVideogameId());
        
        dao.create(nueva);
    }
    
    public void deleteVideogameCategory(Category category, Videogame game) throws SQLException {
        dao.deleteComposed(String.valueOf(game.getVideogameId()), String.valueOf(category.getCategoryId()), "game_id", "category_id");
    }
}
