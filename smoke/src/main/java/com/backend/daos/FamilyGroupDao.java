/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backend.daos;

import com.backend.crud.Crud;
import com.backend.entities.FamilyGroup;
import com.backend.exceptions.AlreadyExistException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author antho
 */
public class FamilyGroupDao extends Crud<FamilyGroup> {

    public FamilyGroupDao(String tabla, String codigo) {
        super(tabla, codigo);
    }

    @Override
    public void create(FamilyGroup entidad) throws SQLException, AlreadyExistException {
       String sql = "INSERT INTO "+tabla+" (group_name, owner_id) VALUES (?,?)";
       
       PreparedStatement stmt = CONNECTION.prepareStatement(sql);
       
       String checkSql = "SELECT * FROM " + tabla + " WHERE group_name = ? AND owner_id = ?";
       PreparedStatement checkStmt = CONNECTION.prepareStatement(checkSql);
       checkStmt.setString(1, entidad.getGroupName());
       checkStmt.setString(2, entidad.getOwnerId());
       ResultSet rs = checkStmt.executeQuery();
       if (rs.next()) {
           throw new AlreadyExistException();
       }
       
       stmt.setString(1, entidad.getGroupName());
       stmt.setString(2, entidad.getOwnerId());
       
       stmt.executeUpdate();
    }

    @Override
    public FamilyGroup obtenerEntidad(ResultSet rs) throws SQLException {
        FamilyGroup group = new FamilyGroup();
        
        group.setGroupId(rs.getInt("group_id"));
        group.setGroupName(rs.getString("group_name"));
        group.setOwnerId(rs.getString("owner_id"));
        
        return group;
    }

    public List<FamilyGroup> listByOwner(String ownerId) throws SQLException {
        String sql = "SELECT * FROM " + tabla + " WHERE owner_id = ?";
        PreparedStatement stmt = CONNECTION.prepareStatement(sql);
        stmt.setString(1, ownerId);
        ResultSet rs = stmt.executeQuery();
        List<FamilyGroup> groups = new ArrayList<>();
        while (rs.next()) {
            groups.add(obtenerEntidad(rs));
        }
        return groups;
    }

    /**
     * Obtiene juegos compartidos dentro de un grupo familiar.
     * Muestra juegos comprados por miembros del grupo que podrían ser usados por otros.
     */
    public com.google.gson.JsonArray getFamilySharedGames(int familyGroupId) throws SQLException {
        String sql = "SELECT DISTINCT vg.videogame_id, vg.name, vg.description, vg.price, " +
                "p.user_id AS purchaser_id, p.date AS purchase_date, " +
                "COALESCE(AVG(r.stars), 0) as community_rating, " +
                "COUNT(DISTINCT r.user_id) as rating_count " +
                "FROM purcharse p " +
                "JOIN videogame vg ON p.game_id = vg.videogame_id " +
                "JOIN groupMember gm ON p.user_id = gm.user_id " +
                "LEFT JOIN rate r ON vg.videogame_id = r.game_id " +
                "WHERE gm.familyGroup_id = ? " +
                "GROUP BY vg.videogame_id, vg.name, vg.description, vg.price, p.user_id, p.date " +
                "ORDER BY p.date DESC";
        
        PreparedStatement stmt = CONNECTION.prepareStatement(sql);
        stmt.setInt(1, familyGroupId);
        ResultSet rs = stmt.executeQuery();
        
        com.google.gson.JsonArray arr = new com.google.gson.JsonArray();
        while (rs.next()) {
            com.google.gson.JsonObject obj = new com.google.gson.JsonObject();
            obj.addProperty("gameId", rs.getInt("videogame_id"));
            obj.addProperty("gameTitle", rs.getString("name"));
            obj.addProperty("description", rs.getString("description"));
            obj.addProperty("price", rs.getDouble("price"));
            obj.addProperty("purchaserId", rs.getString("purchaser_id"));
            obj.addProperty("purchaseDate", rs.getDate("purchase_date").toString());
            obj.addProperty("communityRating", rs.getDouble("community_rating"));
            obj.addProperty("ratingCount", rs.getInt("rating_count"));
            arr.add(obj);
        }
        return arr;
    }
    
}
