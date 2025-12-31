/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backend.gamers;

import com.backend.daos.GamerDao;
import com.backend.entities.Gamer;
import java.sql.SQLException;

/**
 *
 * @author antho
 */
public class Recharge {
    
    public void recharge(Gamer gamer, double money) throws SQLException {
        GamerDao dao = new GamerDao("gamer", "user_id");
        dao.update(gamer.getMail(), "wallet", (gamer.getWallet() + money));
    }
    
}
