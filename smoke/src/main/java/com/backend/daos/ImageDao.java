/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backend.daos;

import com.backend.crud.Crud;
import com.backend.entities.Image;
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
public class ImageDao extends Crud<Image> {

    public ImageDao(String tabla, String codigo) {
        super(tabla, codigo);
    }

    @Override
    public void create(Image entidad) throws SQLException, AlreadyExistException {
        String sql = "INSERT INTO "+tabla+" (image, game_id) VALUES (?,?)";
        
        PreparedStatement stmt = CONNECTION.prepareStatement(sql);
       
        stmt.setBytes(1, entidad.getImage());
        stmt.setInt(2, entidad.getGameId());
        
        stmt.executeUpdate();
    }

    @Override
    public Image obtenerEntidad(ResultSet rs) throws SQLException {
        Image image = new Image();
        
        image.setImageId(rs.getInt("image_id"));
        image.setGameId(rs.getInt("game_id"));
        image.setImage(rs.getBytes("image"));
        
        return image;
    }
    
    public List<Image> readByGameId(int gameId) throws SQLException {
        String sql = "SELECT * FROM " + tabla + " WHERE game_id = ?";
        PreparedStatement stmt = CONNECTION.prepareStatement(sql);
        stmt.setInt(1, gameId);
        ResultSet rs = stmt.executeQuery();
        List<Image> images = new ArrayList<>();
        while (rs.next()) {
            images.add(obtenerEntidad(rs));
        }
        return images;
    }
}
