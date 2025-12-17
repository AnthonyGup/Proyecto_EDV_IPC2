/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backend.daos;

import com.backend.crud.Crud;
import com.backend.entities.Comment;
import com.backend.exceptions.AlreadyExistException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
        String sql = "INSERT INTO" +tabla+ "(user_id, text, gme_id, parent_id) VALUES (?,?,?,?)";
        PreparedStatement stmt = CONNECTION.prepareStatement(sql);
        
        stmt.setString(1, entidad.getUserId());
        stmt.setInt(2, entidad.getGameId());
        stmt.setString(3, entidad.getText());
        stmt.setInt(4, entidad.getParentId());
        
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
        
        return comment;
    }
    
}
