/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backend.gamers;

import com.backend.daos.LibraryDao;
import com.backend.entities.Gamer;
import com.backend.entities.Library;
import com.backend.entities.Videogame;
import com.backend.exceptions.AlreadyExistException;
import com.backend.validators.ValidationException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author antho
 */
public class Install {

    private Gamer gamer;
    private Videogame game;

    public Install(Gamer gamer, Videogame game) {
        this.game = game;
        this.gamer = gamer;
    }

    public boolean install() throws ValidationException, SQLException, AlreadyExistException {
        LibraryDao dao = new LibraryDao("`library`", "library_id");
        List<Library> libraries = dao.readByGamer(gamer.getMail());

        // Verificar si el juego específico está en la biblioteca
        Library target = libraries.stream()
                .filter(lib -> lib.getGameId() == game.getVideogameId())
                .findFirst()
                .orElse(null);

        if (target == null) {
            throw new ValidationException("El juego no está en la biblioteca del gamer");
        }

        if (target.isBuyed()) {
            // Para juegos comprados, solo instalar el existente
            dao.update(target.getLibraryId(), "installed", true);
        } else {
            // Para juegos prestados, verificar si hay otro prestado instalado
            List<Library> otherBorrowedInstalled = libraries.stream()
                    .filter(lib -> lib.isInstalled() && !lib.isBuyed() && lib.getGameId() != game.getVideogameId())
                    .collect(Collectors.toList());
            
            if (!otherBorrowedInstalled.isEmpty()) {
                // Desinstalar todos los prestados instalados
                for (Library lib : otherBorrowedInstalled) {
                    dao.update(lib.getLibraryId(), "installed", false);
                }
                // Mensaje informativo (el servlet puede capturar esto)
                String uninstalledIds = otherBorrowedInstalled.stream()
                        .map(lib -> String.valueOf(lib.getGameId()))
                        .collect(Collectors.joining(", "));
                throw new ValidationException("BORROWED_CONFLICT:" + uninstalledIds);
            }
            
            // Instalar el juego prestado
            dao.update(target.getLibraryId(), "installed", true);
        }
        return true;
    }
}
