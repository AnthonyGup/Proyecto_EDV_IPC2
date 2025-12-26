/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backend.images;

import com.backend.daos.ImageDao;
import com.backend.entities.Image;
import com.backend.entities.Videogame;
import com.backend.exceptions.AlreadyExistException;

import java.sql.SQLException;

/**
 *
 * @author antho
 */
public class ImageManagement {

    public void addImage(Videogame game, Image image) throws SQLException, AlreadyExistException {
        image.setGameId(game.getVideogameId());
        ImageDao dao = new ImageDao("image", "image_id");
        dao.create(image);
    }

    public void removeImage(Videogame game, Image image) throws SQLException {
        if (image.getGameId() == game.getVideogameId()) {
            ImageDao dao = new ImageDao("image", "image_id");
            dao.delete(String.valueOf(image.getImageId()));
        }
    }
}
