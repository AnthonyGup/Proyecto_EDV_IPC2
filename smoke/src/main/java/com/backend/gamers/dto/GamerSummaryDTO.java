package com.backend.gamers.dto;

public class GamerSummaryDTO {
    public String mail;
    public String nickname;
    public String country;
    public int phone;

    public GamerSummaryDTO(String mail, String nickname, String country, int phone) {
        this.mail = mail;
        this.nickname = nickname;
        this.country = country;
        this.phone = phone;
    }
}
