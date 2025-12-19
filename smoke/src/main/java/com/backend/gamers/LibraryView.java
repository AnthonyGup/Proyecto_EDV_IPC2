/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backend.gamers;

import com.backend.daos.CategoryDao;
import com.backend.daos.LibraryDao;
import com.backend.daos.VideogameCategoryDao;
import com.backend.daos.VideogameDao;
import com.backend.entities.Category;
import com.backend.entities.Library;
import com.backend.entities.Videogame;
import com.backend.entities.VideogameCategory;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author antho
 */
public class LibraryView {

    public List<Videogame> getGamesByUserAndFilter(String userId, String filterType, String filterValue) throws SQLException {
        LibraryDao libDao = new LibraryDao("library", "library_id");
        List<Library> libs = libDao.readByGamer(userId);
        
        List<Videogame> result = new ArrayList<>();
        VideogameDao vgDao = new VideogameDao("videogame", "videogame_id");
        
        for (Library lib : libs) {
            Videogame vg = vgDao.readByPk(String.valueOf(lib.getGameId()));
            if (vg != null) {
                boolean matches = false;
                if ("name".equals(filterType)) {
                    matches = vg.getName().toLowerCase().contains(filterValue.toLowerCase());
                } else if ("category".equals(filterType)) {
                    CategoryDao catDao = new CategoryDao("category", "category_id");
                    Category cat = catDao.readByColumn(filterValue, "name");
                    if (cat != null) {
                        VideogameCategoryDao vcDao = new VideogameCategoryDao("videogameCategory", "videogameCategory_id");
                        List<VideogameCategory> vcs = vcDao.readByGameId(lib.getGameId());
                        matches = vcs.stream().anyMatch(vc -> vc.getCategoryId() == cat.getCategoryId());
                    }
                }
                if (matches) {
                    result.add(vg);
                }
            }
        }
        
        return result;
    }
}
