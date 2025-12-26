/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backend.banner;

import com.backend.daos.PurcharseDao;
import com.backend.daos.RateDao;
import com.backend.daos.VideogameDao;
import com.backend.entities.Purcharse;
import com.backend.entities.Rate;
import com.backend.entities.Videogame;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author antho
 */
public class BannerManagement {
    
    public List<Videogame> updateBannerGames() throws SQLException {
        VideogameDao dao = new VideogameDao("videogame", "videogame_id");
        List<Videogame> todos = dao.readAll();
        
        Videogame[] games = new Videogame[5];
        double[] temp = new double[5];
        int index = 0;
        for (Videogame gam : todos) {
            if (gam != null) {
                double val = calcularValor(gam);
                if (val > temp[index]) {
                    games[index] = gam;
                    temp[index] = val;
                }
                if (index > 3) {
                    index = 0;
                }
                index++;
            }
        }
        List<Videogame> banner = new ArrayList<>();
        for (Videogame game : games) {
            if (game != null) {
                banner.add(game);
            }
        }
        return banner;
    }
    
    private double calcularValor(Videogame game) throws SQLException {
        RateDao daoRate = new RateDao("rate", "rate_id");
        PurcharseDao daoVntas = new PurcharseDao("purcharse", "purcharse_id");
        List<Rate> rates = daoRate.readAll();
        List<Purcharse>  ventas = daoVntas.readAll();
        
        int cantidadRate = 0;
        
        int mediaRate = 0;
        int ventasTotales = 0;
        
        for (Rate rate: rates) {
            if (rate.getGameId() == game.getVideogameId()) {
                cantidadRate++;
                mediaRate = mediaRate + rate.getStars();
            }
        }
        for (Purcharse venta : ventas) {
            if (venta.getGameId() == game.getVideogameId()) {
                ventasTotales++;
            }
        }
        return calcular(mediaRate, ventasTotales);
    }
    
    private double calcular(double rate, double ventas) {
        double izq = ((0.6*(rate/5))+(0.4*(ventas/(ventas + 20* Math.log(ventas + 10)))));
        double der =  (1 - Math.abs((rate/5)-(ventas/(ventas + 20* Math.log(ventas + 10)))));
        
        return  izq * der;
    }
    
}
