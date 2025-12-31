package com.backend.categories;

import com.backend.daos.CategoryDao;
import com.backend.daos.VideogameCategoryDao;
import com.backend.db.DBConnection;
import com.backend.entities.Category;
import com.backend.entities.VideogameCategory;
import com.backend.exceptions.AlreadyExistException;
import com.backend.exceptions.ServiceException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class CategoryService {

    private final CategoryDao categoryDao = new CategoryDao("category", "category_id");
    private final VideogameCategoryDao vgCatDao = new VideogameCategoryDao("videogameCategory", "videogameCategory_id");

    public List<Category> listAll() throws SQLException {
        return categoryDao.readAll();
    }

    public Category getById(String id) throws SQLException, ServiceException {
        if (id == null || id.isBlank()) {
              throw new ServiceException(400, "Category ID requerido");
        }
        Category cat = categoryDao.readByPk(id);
        if (cat == null) {
              throw new ServiceException(404, "Categoria no encontrada");
        }
        return cat;
    }

    public Category create(Category incoming) throws SQLException, AlreadyExistException, ServiceException {
        if (incoming == null || incoming.getName() == null || incoming.getName().isBlank()) {
              throw new ServiceException(400, "Nombre de categoria requerido");
        }
        categoryDao.create(incoming);
        Category created = categoryDao.readByColumn(incoming.getName(), "name");
        if (created == null) {
              throw new ServiceException(500, "No fue posible recuperar la categoria creada");
        }
        return created;
    }

    public Category updateName(String id, String newName) throws SQLException, AlreadyExistException, ServiceException {
        if (id == null || id.isBlank()) {
            throw new ServiceException(400, "Category ID requerido");
        }
        if (newName == null || newName.isBlank()) {
              throw new ServiceException(400, "Nombre de categoria requerido");
        }
        Category existing = categoryDao.readByColumn(newName, "name");
        if (existing != null && !(String.valueOf(existing.getCategoryId()).equals(id))) {
              throw new ServiceException(409, "La categoria ya existe");
        }
        categoryDao.update(id, "name", newName);
        Category updated = categoryDao.readByPk(id);
        if (updated == null) {
              throw new ServiceException(404, "Categoria no encontrada");
        }
        return updated;
    }

    public void delete(String id) throws SQLException, ServiceException {
        if (id == null || id.isBlank()) {
            throw new ServiceException(400, "Category ID requerido");
        }
        categoryDao.delete(id);
    }

    public List<Category> getByGameId(int gameId) throws SQLException {
        List<VideogameCategory> links = vgCatDao.readByGameId(gameId);
        List<Category> categories = new ArrayList<>();
        for (VideogameCategory link : links) {
            Category cat = categoryDao.readByPk(String.valueOf(link.getCategoryId()));
            if (cat != null) {
                categories.add(cat);
            }
        }
        return categories;
    }

    public GameCategoriesUpdateResult replaceGameCategories(int gameId, List<Integer> categoryIds) throws SQLException {
        Connection conn = DBConnection.getInstance().getConnection();

        int deletedCount;
        try (PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM videogameCategory WHERE game_id = ?")) {
            deleteStmt.setInt(1, gameId);
            deletedCount = deleteStmt.executeUpdate();
        }

        int insertedCount = 0;
        if (categoryIds != null) {
            try (PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO videogameCategory (game_id, category_id) VALUES (?, ?)")) {
                for (Integer categoryId : categoryIds) {
                    insertStmt.setInt(1, gameId);
                    insertStmt.setInt(2, categoryId);
                    insertStmt.executeUpdate();
                    insertedCount++;
                }
            }
        }

        return new GameCategoriesUpdateResult(deletedCount, insertedCount);
    }
}
