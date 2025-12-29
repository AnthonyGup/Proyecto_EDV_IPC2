package com.backend.games.servlets;

public class BuyResponse {
    public String message;
    public double wallet;

    public BuyResponse(String message, double wallet) {
        this.message = message;
        this.wallet = wallet;
    }
}
