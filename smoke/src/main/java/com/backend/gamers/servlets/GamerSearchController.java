package com.backend.gamers.servlets;

import com.backend.daos.GamerDao;
import com.backend.entities.Gamer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "GamerSearchController", urlPatterns = {"/gamer/search"})
public class GamerSearchController extends HttpServlet {

    private final Gson gson = new GsonBuilder().create();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String q = request.getParameter("q");
        if (q == null || q.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Parámetro q requerido\"}");
            return;
        }

        try {
            GamerDao dao = new GamerDao("gamer", "user_id");
            List<Gamer> gamers = dao.searchByNickname(q.trim());

            // Map to lightweight DTO
            List<GamerSummaryDTO> dtos = new ArrayList<>();
            for (Gamer g : gamers) {
                dtos.add(new GamerSummaryDTO(g.getMail(), g.getNickname(), g.getCountry(), g.getPhone()));
            }

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(dtos));
        } catch (SQLException ex) {
            Logger.getLogger(GamerSearchController.class.getName()).log(Level.SEVERE, null, ex);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Error en base de datos\"}");
        }
    }

    static class GamerSummaryDTO {
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
}
