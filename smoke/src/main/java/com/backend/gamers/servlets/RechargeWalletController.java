package com.backend.gamers.servlets;

import com.backend.daos.GamerDao;
import com.backend.entities.Gamer;
import com.backend.gamers.Recharge;
import com.backend.gamers.dto.RechargeRequest;
import com.backend.gamers.dto.RechargeResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "RechargeWalletController", urlPatterns = {"/gamer/recharge"})
public class RechargeWalletController extends HttpServlet {

    private final Gson gson = new GsonBuilder().create();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            // Parsear el body para obtener el monto y email
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }

            RechargeRequest rechargeReq = gson.fromJson(sb.toString(), RechargeRequest.class);
            if (rechargeReq == null || rechargeReq.amount <= 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\":\"Invalid amount\"}");
                return;
            }

            // Obtener email desde el request
            String userEmail = rechargeReq.userEmail;
            if (userEmail == null || userEmail.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\":\"User email not provided\"}");
                return;
            }

            // Obtener el gamer actual
            GamerDao gamerDao = new GamerDao("gamer", "user_id");
            Gamer gamer = gamerDao.readByPk(userEmail);

            if (gamer == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"error\":\"Gamer not found\"}");
                return;
            }

            // Realizar la recarga
            Recharge recharge = new Recharge();
            recharge.recharge(gamer, rechargeReq.amount);

            // Obtener el nuevo wallet
            Gamer updatedGamer = gamerDao.readByPk(userEmail);
            
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(new RechargeResponse("Wallet recargado exitosamente", updatedGamer.getWallet())));

        } catch (SQLException ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"" + ex.getMessage() + "\"}");
        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Error processing request: " + ex.getMessage() + "\"}");
        }
    }

}
