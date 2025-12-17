/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backend.entities;

/**
 *
 * @author antho
 */
public class Library {
    private String libraryId;
    private String gamer_id;
    private int gameId;
    private boolean installed = false;
    private boolean buyed = false;

    public String getGamer_id() {
        return gamer_id;
    }

    public void setGamer_id(String gamer_id) {
        this.gamer_id = gamer_id;
    }
    
    public String getLibraryId() {
        return libraryId;
    }

    public void setLibraryId(String libraryId) {
        this.libraryId = libraryId;
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public boolean isInstalled() {
        return installed;
    }

    public void setInstalled(boolean installed) {
        this.installed = installed;
    }

    public boolean isBuyed() {
        return buyed;
    }

    public void setBuyed(boolean buyed) {
        this.buyed = buyed;
    }
    
    
}
