package com.backend.gamers.dto;

public class GamerInfoDTO {
    public String userEmail;
    public String nickname;
    public String country;
    public int phone;
    public double wallet;

    public GamerInfoDTO(String userEmail, String nickname, String country, int phone, double wallet) {
        this.userEmail = userEmail;
        this.nickname = nickname;
        this.country = country;
        this.phone = phone;
        this.wallet = wallet;
    }
}
