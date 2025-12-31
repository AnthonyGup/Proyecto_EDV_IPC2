package com.backend.games.servlets;

import com.backend.entities.Gamer;
import com.backend.entities.Videogame;
import com.backend.daos.GamerDao;
import com.backend.daos.VideogameDao;
import com.backend.gamers.Buy;
import com.backend.entities.Purcharse;
import com.backend.entities.Library;
import com.backend.daos.PurcharseDao;
import com.backend.daos.LibraryDao;
import com.backend.daos.GroupMemberDao;
import com.backend.entities.FamilyGroup;
import com.backend.db.DBConnection;
import java.sql.Connection;
import com.backend.exceptions.AlreadyExistException;
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
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "GameBuyController", urlPatterns = {"/game/buy"})
public class GameBuyController extends HttpServlet {

    private final Gson gson = new GsonBuilder().create();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            // Parsear el body
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }

            BuyRequest buyReq = gson.fromJson(sb.toString(), BuyRequest.class);
            if (buyReq == null || buyReq.videogameId <= 0 || buyReq.userEmail == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\":\"Invalid parameters\"}");
                return;
            }

            // Obtener gamer
            GamerDao gamerDao = new GamerDao("gamer", "user_id");
            Gamer gamer = gamerDao.readByPk(buyReq.userEmail);
            if (gamer == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"error\":\"Gamer not found\"}");
                return;
            }

            // Obtener videojuego
            VideogameDao gameDao = new VideogameDao("videogame", "videogame_id");
            Videogame game = gameDao.readByPk(buyReq.videogameId + "");
            if (game == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"error\":\"Game not found\"}");
                return;
            }

            PurcharseDao purchaseDao = new PurcharseDao("purcharse", "purcharse_id");

            // Validar si ya existe una compra previa de este juego por el usuario
            if (purchaseDao.existsByUserAndGame(gamer.getMail(), game.getVideogameId())) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(new BuyResponse("Ya has comprado este juego anteriormente", gamer.getWallet())));
                return;
            }

            LibraryDao libraryDao = new LibraryDao("`library`", "library_id");

            // Transacción: compra + actualizar/insertar en biblioteca
            Connection conn = DBConnection.getInstance().getConnection();
            boolean prevAutoCommit = conn.getAutoCommit();
            try {
                conn.setAutoCommit(false);

                // Realizar la compra
                Buy buy = new Buy(gamer, game);
                Purcharse purchase = buy.buy();
                purchaseDao.create(purchase);

                // Verificar si el juego ya está en la biblioteca (prestado del grupo)
                java.util.List<Library> libs = libraryDao.readByGamer(gamer.getMail());
                Library existingLib = libs.stream()
                    .filter(l -> l.getGameId() == game.getVideogameId())
                    .findFirst()
                    .orElse(null);

                if (existingLib != null) {
                    // Ya existe en library (prestado), actualizar a buyed=true
                    libraryDao.update(existingLib.getLibraryId(), "buyed", true);
                } else {
                    // No existe, crear nueva entrada con buyed=true
                    Library lib = new Library();
                    lib.setGamer_id(gamer.getMail());
                    lib.setGameId(game.getVideogameId());
                    lib.setBuyed(true);
                    lib.setInstalled(false);
                    libraryDao.create(lib);
                }
                
                conn.commit();
            } catch (Exception libEx) {
                try {
                    conn.rollback();
                } catch (Exception ignore) {
                    // best effort rollback
                }
                throw libEx;
            } finally {
                try {
                    conn.setAutoCommit(prevAutoCommit);
                } catch (Exception ignore) {
                    // best effort restore
                }
            }

            // Obtener el nuevo wallet actualizado
            Gamer updatedGamer = gamerDao.readByPk(buyReq.userEmail);
            double newWallet = updatedGamer != null ? updatedGamer.getWallet() : gamer.getWallet();

            // Sincronizar bibliotecas de los grupos a los que pertenece el usuario
            try {
                GroupMemberDao gmDao = new GroupMemberDao("groupMember", "");
                java.util.List<FamilyGroup> groups = gmDao.listGroupsByUser(gamer.getMail());
                for (FamilyGroup fg : groups) {
                    try {
                        gmDao.syncLibrariesForGroup(fg.getGroupId());
                    } catch (SQLException syncEx) {
                        // Registrar y continuar para no bloquear la compra
                        Logger.getLogger(GameBuyController.class.getName()).log(Level.WARNING, "Sync libraries failed for group " + fg.getGroupId(), syncEx);
                    }
                }
            } catch (SQLException ex) {
                Logger.getLogger(GameBuyController.class.getName()).log(Level.WARNING, "Failed to list groups for sync", ex);
            }

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(new BuyResponse("Juego comprado exitosamente", newWallet)));

        } catch (com.backend.validators.ValidationException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"" + ex.getMessage() + "\"}");
        } catch (AlreadyExistException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Ya posees este juego\"}");
        } catch (SQLException ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Database error: " + ex.getMessage() + "\"}");
            Logger.getLogger(GameBuyController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Error processing request: " + ex.getMessage() + "\"}");
        }
    }
}

