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
public class Videogame {
    
    private int videogameId;
    private LocalDate relasedate;
    private String minimRequirements;
    private double price;
    private String description;
    private String name;
    private int companyId;
    private int ageRestriction;
    private boolean available;

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public int getVideogameId() {
        return videogameId;
    }

    public void setVideogameId(int videogameId) {
        this.videogameId = videogameId;
    }

    public LocalDate getRelasedate() {
        return relasedate;
    }

    public void setRelasedate(LocalDate relasedate) {
        this.relasedate = relasedate;
    }

    public String getMinimRequirements() {
        return minimRequirements;
    }

    public void setMinimRequirements(String minimRequirements) {
        this.minimRequirements = minimRequirements;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public int getAgeRestriction() {
        return ageRestriction;
    }

    public void setAgeRestriction(int ageRestriction) {
        this.ageRestriction = ageRestriction;
    }
    
    
    
}
