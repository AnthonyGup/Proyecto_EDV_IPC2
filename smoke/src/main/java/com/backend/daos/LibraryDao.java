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
        String sql = "INSERT INTO "+tabla+" (library_id, gamer_id, game_id, buyed, installed) VALUES (?,?,?,?,?)";
        
        PreparedStatement stmt = CONNECTION.prepareStatement(sql);
        
        stmt.setString(1, entidad.getGamer_id() + entidad.getGameId());
        stmt.setString(2, entidad.getGamer_id());
        stmt.setInt(3, entidad.getGameId());
        stmt.setBoolean(4, entidad.isBuyed());
        stmt.setBoolean(5, entidad.isInstalled());
        
        if (readByPk(entidad.getGamer_id() + entidad.getGameId()) != null) {
            throw new AlreadyExistException();
        }
        
        stmt.executeUpdate();
    }

    @Override
    public Library obtenerEntidad(ResultSet rs) throws SQLException {
        Library library = new Library();
        
        library.setLibraryId(rs.getString("library_id"));
        library.setGamer_id(rs.getString("gamer_id"));
        library.setGameId(rs.getInt("game_id"));
        library.setBuyed(rs.getBoolean("buyed"));
        library.setInstalled(rs.getBoolean("installed"));
        
        return library;
    }
    
}
