package com.backend.games.servlets;

import com.backend.daos.CompanyDao;
import com.backend.daos.VideogameDao;
import com.backend.db.DBConnection;
import com.backend.entities.Company;
import com.backend.entities.Videogame;
import com.backend.extras.LocalDateAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "CompanyGamesController", urlPatterns = {"/company/games/*"})
public class CompanyGamesController extends HttpServlet {

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Company ID requerido\"}");
            return;
        }

        try {
            int companyId = Integer.parseInt(pathInfo.substring(1));
            VideogameDao vgDao = new VideogameDao("videogame", "videogame_id");
            
            // Obtener juegos por company_id
            String sql = "SELECT * FROM videogame WHERE company_id = ?";
            PreparedStatement stmt = DBConnection.getInstance().getConnection().prepareStatement(sql);
            stmt.setInt(1, companyId);
            ResultSet rs = stmt.executeQuery();
            
            List<Videogame> games = new ArrayList<>();
            while (rs.next()) {
                games.add(vgDao.obtenerEntidad(rs));
            }
            
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(games));
        } catch (NumberFormatException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"ID de compañía inválido\"}");
        } catch (SQLException ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Error en base de datos\"}");
            Logger.getLogger(CompanyGamesController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
