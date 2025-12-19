/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backend.daos;

import com.backend.crud.Crud;
import com.backend.entities.Library;
import com.backend.exceptions.AlreadyExistException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author antho
 */
public class LibraryDao extends Crud<Library>  {

    public LibraryDao(String tabla, String codigo) {
        super(tabla, codigo);
    }

    @Override
    public void create(Library entidad) throws SQLException, AlreadyExistException {
        String sql = "INSERT INTO "+tabla+" (user_id, game_id, buyed, installed) VALUES (?,?,?,?)";
        
        PreparedStatement stmt = CONNECTION.prepareStatement(sql);
        
        stmt.setString(1, entidad.getGamer_id());
        stmt.setInt(2, entidad.getGameId());
        stmt.setBoolean(3, entidad.isBuyed());
        stmt.setBoolean(4, entidad.isInstalled());
        
        stmt.executeUpdate();
    }

    @Override
    public Library obtenerEntidad(ResultSet rs) throws SQLException {
        Library library = new Library();
        
        library.setLibraryId(String.valueOf(rs.getInt("library_id")));
        library.setGamer_id(rs.getString("user_id"));
        library.setGameId(rs.getInt("game_id"));
        library.setBuyed(rs.getBoolean("buyed"));
        library.setInstalled(rs.getBoolean("installed"));
        
        return library;
    }
    
    public List<Library> readByGamer(String gamerId) throws SQLException {
        List<Library> libraries = new ArrayList<>();
        String sql = "SELECT * FROM " + tabla + " WHERE user_id = ?";
        
        PreparedStatement stmt = CONNECTION.prepareStatement(sql);
        stmt.setString(1, gamerId);
        ResultSet rs = stmt.executeQuery();
        
        while (rs.next()) {
            libraries.add(obtenerEntidad(rs));
        }
        
        return libraries;
    }
}
