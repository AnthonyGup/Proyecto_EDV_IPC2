/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backend.entities;

import java.time.LocalDate;

/**
 *
 * @author antho
 */
public class Purcharse {
    private String userId;
    private LocalDate date;
    private double price;
    private int purcharseId;
    private int gameId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getPurcharseId() {
        return purcharseId;
    }

    public void setPurcharseId(int purcharseId) {
        this.purcharseId = purcharseId;
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }
}
