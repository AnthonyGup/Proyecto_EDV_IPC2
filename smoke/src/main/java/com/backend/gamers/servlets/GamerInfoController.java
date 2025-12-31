package com.backend.gamers.servlets;

import com.backend.daos.GamerDao;
import com.backend.entities.Gamer;
import com.backend.gamers.dto.GamerInfoDTO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "GamerInfoController", urlPatterns = {"/gamer/info/*"})
public class GamerInfoController extends HttpServlet {

    private final Gson gson = new GsonBuilder().create();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            // Obtener el email desde la URL
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.length() <= 1) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\":\"Email not provided\"}");
                return;
            }

            String userEmail = pathInfo.substring(1); // Remover el slash inicial

            // Obtener el gamer por email
            GamerDao gamerDao = new GamerDao("gamer", "user_id");
            Gamer gamer = gamerDao.readByPk(userEmail);

            if (gamer == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"error\":\"Gamer not found\"}");
                return;
            }

            GamerInfoDTO gamerInfo = new GamerInfoDTO(
                gamer.getMail(),
                gamer.getNickname(),
                gamer.getCountry(),
                gamer.getPhone(),
                gamer.getWallet()
            );

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(gamerInfo));

        } catch (SQLException ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"" + ex.getMessage() + "\"}");
        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Error processing request: " + ex.getMessage() + "\"}");
        }
    }

}
