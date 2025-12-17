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
        String sql = "INSERT INTO "+tabla+" (familyGroup_id, user_id, member_id) VALUES (?,?,?)";
        
        PreparedStatement stmt = CONNECTION.prepareStatement(sql);
        
        String  idCompuesta = entidad.getFamilyGroupId() + entidad.getMemberId();
        
        if (readByPk(idCompuesta) != null) {
            throw new AlreadyExistException();
        }
        
        stmt.setString(1, idCompuesta);
        stmt.setString(2, entidad.getMemberId());
        stmt.setInt(3, entidad.getFamilyGroupId());
    }

    @Override
    public GroupMember obtenerEntidad(ResultSet rs) throws SQLException {
        GroupMember member = new GroupMember();
        
        member.setFamilyGroupId(rs.getInt("familyGroup_id"));
        member.setMemberId(rs.getString("user_id"));
        member.setMemberId(rs.getString("member_id"));
        
        return member;
    }
    
}
