/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backend.gamers;

import com.backend.daos.GamerDao;
import com.backend.entities.Gamer;
import com.backend.entities.Purcharse;
import com.backend.entities.Videogame;
import com.backend.validators.ValidationException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;

/**
 *
 * @author antho
 */
public class Buy {

    private Gamer gamer;
    private Videogame game;
    
    public Buy(Gamer gamer, Videogame game) {
        this.game =  game;
        this.gamer = gamer;
    }
    
    public Purcharse buy() throws ValidationException, SQLException {
        if (!isAge() || !haveMoney()) {
            throw new ValidationException("Error al realizar la compra");
        }
        Purcharse compra = new Purcharse();
        compra.setUserId(gamer.getMail());
        compra.setDate(LocalDate.now());
        compra.setPrice(game.getPrice());
        compra.setGameId(game.getVideogameId());
        
        cobrar();
        
        return compra;
    }
    
    private boolean isAge() {
        LocalDate hoy = LocalDate.now();
        Period periodo = Period.between(gamer.getBirthdate(), hoy);
        
        int edad = periodo.getYears();
        
        if (edad >= game.getAgeRestriction()) {
            return true;
        }
        return false;
    }
    
    private boolean haveMoney() {
        double userMoney = gamer.getWallet();
        double price = game.getPrice();
        if (userMoney >= price) {
            return true;
        }
        return false;
    }
    
    private void cobrar() throws SQLException {
        double userMoney = gamer.getWallet();
        double price = game.getPrice();
        
        double total =  userMoney - price;
        
        GamerDao dao = new GamerDao("gamer", "user_id");
        dao.update(gamer.getMail(), "wallet", total);
    }
}
