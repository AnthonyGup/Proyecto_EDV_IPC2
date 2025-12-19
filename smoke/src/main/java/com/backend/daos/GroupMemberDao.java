/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backend.daos;

import com.backend.crud.Crud;
import com.backend.entities.GroupMember;
import com.backend.exceptions.AlreadyExistException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author antho
 */
public class GroupMemberDao extends Crud<GroupMember> {

    public GroupMemberDao(String tabla, String codigo) {
        super(tabla, codigo);
    }

    @Override
    public void create(GroupMember entidad) throws SQLException, AlreadyExistException {
        String sql = "INSERT INTO "+tabla+" (familyGroup_id, user_id) VALUES (?,?)";
        
        PreparedStatement stmt = CONNECTION.prepareStatement(sql);
        
        // ver si existe ya un miembr en ese grupo
        String checkSql = "SELECT * FROM " + tabla + " WHERE familyGroup_id = ? AND user_id = ?";
        PreparedStatement checkStmt = CONNECTION.prepareStatement(checkSql);
        checkStmt.setInt(1, entidad.getFamilyGroupId());
        checkStmt.setString(2, entidad.getUserId());
        ResultSet rs = checkStmt.executeQuery();
        if (rs.next()) {
            throw new AlreadyExistException();
        }
        
        stmt.setInt(1, entidad.getFamilyGroupId());
        stmt.setString(2, entidad.getUserId());
        
        stmt.executeUpdate();
    }

    @Override
    public GroupMember obtenerEntidad(ResultSet rs) throws SQLException {
        GroupMember member = new GroupMember();
        
        member.setFamilyGroupId(rs.getInt("familyGroup_id"));
        member.setUserId(rs.getString("user_id"));
        
        return member;
    }
    
}
