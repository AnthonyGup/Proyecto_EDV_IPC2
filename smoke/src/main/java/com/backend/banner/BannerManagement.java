/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backend.banner;

import com.backend.daos.VideogameDao;
import com.backend.db.DBConnection;
import com.backend.entities.Videogame;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author antho
 */
public class BannerManagement {
    
    public List<Videogame> updateBannerGames() throws SQLException {
        return getTopGames(5);
    }
    
    public List<Videogame> getTopGames(int limit) throws SQLException {
        VideogameDao dao = new VideogameDao("videogame", "videogame_id");
        List<Videogame> todos = dao.readAll();
        
        // Crear mapa temporal de juegos con sus puntuaciones
        Map<Videogame, Double> gameScores = new HashMap<>();
        
        for (Videogame game : todos) {
            if (game != null) {
                double val = calcularValor(game);
                if (!Double.isNaN(val) && !Double.isInfinite(val)) {
                    gameScores.put(game, val);
                }
            }
        }
        
        // Ordenar y obtener los mejores
        List<Videogame> result = new ArrayList<>();
        gameScores.entrySet()
            .stream()
            .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
            .limit(limit)
            .forEach(entry -> result.add(entry.getKey()));
        
        return result;
    }
    
    private double calcularValor(Videogame game) throws SQLException {
        Connection conn = DBConnection.getInstance().getConnection();

        double promedioRate = 0.0;
        int ventasTotales = 0;

        // Obtener cantidad y promedio de estrellas desde la tabla correcta 'rate'
        String sqlRate = "SELECT COUNT(*) AS cnt, COALESCE(AVG(stars), 0) AS avgStars FROM rate WHERE game_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sqlRate)) {
            ps.setInt(1, game.getVideogameId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    promedioRate = rs.getDouble("avgStars");
                }
            }
        }

        // Obtener cantidad de compras desde la tabla correcta 'purcharse'
        String sqlPurchase = "SELECT COUNT(*) AS cnt FROM purcharse WHERE game_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sqlPurchase)) {
            ps.setInt(1, game.getVideogameId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ventasTotales = rs.getInt("cnt");
                }
            }
        }

        return calcular(promedioRate, ventasTotales);
    }
    
    private double calcular(double promedioRate, double ventasTotales) {
        if (ventasTotales == 0) {
            return (promedioRate / 5.0) * 0.6;
        }
        
        if (promedioRate == 0 && ventasTotales == 0) {
            return 0.0;
        }
        
        // Normalizar rating a 0-1
        double rateNorm = promedioRate / 5.0;
        
        // Calcular factor de ventas normalizadas
        double ventasNorm = ventasTotales / (ventasTotales + 20.0 * Math.log(ventasTotales + 10.0));
        
        // Fórmula ponderada: 60% rating, 40% ventas
        double izq = (0.6 * rateNorm) + (0.4 * ventasNorm);
        
        // Factor de coherencia entre rating y ventas
        double der = (1.0 - Math.abs(rateNorm - ventasNorm));
        
        return izq * der;
    }
}
