/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backend.daos;

import com.backend.crud.Crud;
import com.backend.entities.Comment;
import com.backend.exceptions.AlreadyExistException;
import com.backend.comments.CommentThread;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author antho
 */
public class CommentDao extends Crud<Comment> {

    public CommentDao(String tabla, String codigo) {
        super(tabla, codigo);
    }

    @Override
    public void create(Comment entidad) throws SQLException, AlreadyExistException {
        String sql = "INSERT INTO comment (user_id, game_id, text, parent_id) VALUES (?,?,?,?)";
        PreparedStatement stmt = CONNECTION.prepareStatement(sql);
        
        stmt.setString(1, entidad.getUserId());
        stmt.setInt(2, entidad.getGameId());
        stmt.setString(3, entidad.getText());
        if (entidad.getParentId() > 0) {
            stmt.setInt(4, entidad.getParentId());
        } else {
            stmt.setNull(4, java.sql.Types.INTEGER);
        }
        
        stmt.executeUpdate();
        
    }

    @Override
    public Comment obtenerEntidad(ResultSet rs) throws SQLException {
        Comment comment = new Comment();
        
        comment.setUserId(rs.getString("user_id"));
        comment.setGameId(rs.getInt("game_id"));
        comment.setText(rs.getString("text"));
        comment.setParentId(rs.getInt("parent_id"));
        comment.setComentId(rs.getInt("comment_id"));
        comment.setVisible(rs.getBoolean("visible"));
        
        return comment;
    }
    
    public void updateCommentVisibilityByCompany(int companyId, boolean visible) throws SQLException {
        System.out.println("el estado es:" +  visible);
        String sql = "UPDATE comment SET visible = ? WHERE game_id IN (SELECT videogame_id FROM videogame WHERE company_id = ?)";
        try (PreparedStatement stmt = CONNECTION.prepareStatement(sql)) {
            stmt.setBoolean(1, visible);
            stmt.setInt(2, companyId);
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Filas actualizadas: " + rowsAffected);
        }
    }
    
    public void updateCommentVisibilityByGame(int videogameId, boolean visible) throws SQLException {
        System.out.println("Actualizando visibilidad de comentarios para juego " + videogameId + " a: " + visible);
        String sql = "UPDATE comment SET visible = ? WHERE game_id = ?";
        try (PreparedStatement stmt = CONNECTION.prepareStatement(sql)) {
            stmt.setBoolean(1, visible);
            stmt.setInt(2, videogameId);
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Filas actualizadas para juego " + videogameId + ": " + rowsAffected);
        }
    }
    
    public boolean hasVisibleCommentsByGame(int videogameId) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM comment WHERE game_id = ? AND visible = true";
        try (PreparedStatement stmt = CONNECTION.prepareStatement(sql)) {
            stmt.setInt(1, videogameId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count") > 0;
                }
            }
        }
        return false;
    }

    public java.util.List<Comment> readByGameId(int gameId) throws SQLException {
        java.util.List<Comment> comments = new java.util.ArrayList<>();
        String sql = "SELECT * FROM " + tabla + " WHERE game_id = ? AND visible = true";
        try (PreparedStatement stmt = CONNECTION.prepareStatement(sql)) {
            stmt.setInt(1, gameId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    comments.add(obtenerEntidad(rs));
                }
            }
        }
        return comments;
    }

    public List<CommentThread> readThreadedByGameId(int gameId) throws SQLException {
        // Obtener todos los comentarios visibles del juego
        List<Comment> allComments = readByGameId(gameId);
        
        // Crear un mapa para acceso rápido por ID
        Map<Integer, CommentThread> commentMap = new HashMap<>();
        List<CommentThread> rootComments = new ArrayList<>();
        
        // Crear CommentThread para cada comentario
        for (Comment comment : allComments) {
            commentMap.put(comment.getComentId(), new CommentThread(comment));
        }
        
        // Organizar en estructura jerárquica
        for (Comment comment : allComments) {
            CommentThread thread = commentMap.get(comment.getComentId());
            
            if (comment.getParentId() == 0 || comment.getParentId() == -1) {
                // Es un comentario raíz
                rootComments.add(thread);
            } else {
                // Es una respuesta, agregar al comentario padre
                CommentThread parentThread = commentMap.get(comment.getParentId());
                if (parentThread != null) {
                    parentThread.addReply(thread);
                }
            }
        }
        
        return rootComments;
    }

    /**
     * Top usuarios por cantidad de reseñas/comentarios.
     * @param limit máximo de usuarios
     * @return 
     * @throws java.sql.SQLException
     */
    public com.google.gson.JsonArray getTopUsersByComments(int limit) throws SQLException {
        String sql = "SELECT user_id, COUNT(comment_id) AS comments_count "
                + "FROM comment "
                + "GROUP BY user_id "
                + "ORDER BY comments_count DESC "
                + "LIMIT ?";

        PreparedStatement stmt = CONNECTION.prepareStatement(sql);
        stmt.setInt(1, limit);
        ResultSet rs = stmt.executeQuery();

        com.google.gson.JsonArray arr = new com.google.gson.JsonArray();
        while (rs.next()) {
            com.google.gson.JsonObject obj = new com.google.gson.JsonObject();
            obj.addProperty("userId", rs.getString("user_id"));
            obj.addProperty("commentsCount", rs.getInt("comments_count"));
            arr.add(obj);
        }
        return arr;
    }

    /**
     * Obtiene los mejores comentarios (más interacciones/respuestas) de una empresa.
     */
    public com.google.gson.JsonArray getTopCommentsByCompany(int companyId, int limit) throws SQLException {
        String sql = "SELECT c.comment_id, c.user_id, c.game_id, vg.name, c.text as comment_text, " +
                "COUNT(DISTINCT c2.comment_id) as reply_count " +
                "FROM comment c " +
                "JOIN videogame vg ON c.game_id = vg.videogame_id " +
                "LEFT JOIN comment c2 ON c.comment_id = c2.parent_id " +
                "WHERE vg.company_id = ? AND c.parent_id IS NULL " +
                "GROUP BY c.comment_id, c.user_id, c.game_id, vg.name, c.text " +
                "ORDER BY reply_count DESC " +
                "LIMIT ?";
        
        PreparedStatement stmt = CONNECTION.prepareStatement(sql);
        stmt.setInt(1, companyId);
        stmt.setInt(2, limit);
        ResultSet rs = stmt.executeQuery();
        
        com.google.gson.JsonArray arr = new com.google.gson.JsonArray();
        while (rs.next()) {
            com.google.gson.JsonObject obj = new com.google.gson.JsonObject();
            obj.addProperty("commentId", rs.getInt("comment_id"));
            obj.addProperty("userId", rs.getString("user_id"));
            obj.addProperty("gameId", rs.getInt("game_id"));
            obj.addProperty("gameTitle", rs.getString("name"));
            obj.addProperty("commentText", rs.getString("comment_text"));
            obj.addProperty("replyCount", rs.getInt("reply_count"));
            arr.add(obj);
        }
        return arr;
    }
}