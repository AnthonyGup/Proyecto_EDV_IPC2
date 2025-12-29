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
    
}
