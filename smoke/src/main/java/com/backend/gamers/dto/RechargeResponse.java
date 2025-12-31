package com.backend.gamers.dto;

public class RechargeResponse {
    public String message;
    public double newWallet;

    public RechargeResponse(String message, double newWallet) {
        this.message = message;
        this.newWallet = newWallet;
    }
}
