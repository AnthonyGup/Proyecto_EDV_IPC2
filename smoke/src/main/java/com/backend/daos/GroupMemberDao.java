/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backend.daos;

import com.backend.crud.Crud;
import com.backend.entities.FamilyGroup;
import com.backend.entities.GroupMember;
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

        // validar limite de miembros (máximo 6 por grupo)
        String countSql = "SELECT COUNT(*) AS cnt FROM " + tabla + " WHERE familyGroup_id = ?";
        PreparedStatement countStmt = CONNECTION.prepareStatement(countSql);
        countStmt.setInt(1, entidad.getFamilyGroupId());
        ResultSet crs = countStmt.executeQuery();
        int count = 0;
        if (crs.next()) {
            count = crs.getInt("cnt");
        }
        if (count >= 6) {
            // usar un mensaje identificable para manejo en el servlet
            throw new SQLException("GROUP_LIMIT_EXCEEDED");
        }
        
        stmt.setInt(1, entidad.getFamilyGroupId());
        stmt.setString(2, entidad.getUserId());
        
        stmt.executeUpdate();

        // Sincronizar librerías entre todos los miembros del grupo
        try {
            syncLibrariesForGroup(entidad.getFamilyGroupId());
        } catch (SQLException e) {
            // Logica de sincronización no debe bloquear la creación; propagar sólo si es crítico
            throw e;
        }
    }

    public boolean isMember(String userId, int groupId) throws SQLException {
        String sql = "SELECT 1 FROM " + tabla + " WHERE familyGroup_id = ? AND user_id = ?";
        PreparedStatement stmt = CONNECTION.prepareStatement(sql);
        stmt.setInt(1, groupId);
        stmt.setString(2, userId);
        ResultSet rs = stmt.executeQuery();
        return rs.next();
    }

    public List<FamilyGroup> listGroupsByUser(String userId) throws SQLException {
        String sql = "SELECT fg.group_id, fg.group_name, fg.owner_id FROM " + tabla + " gm "
                + "JOIN familyGroup fg ON gm.familyGroup_id = fg.group_id "
                + "WHERE gm.user_id = ?";
        PreparedStatement stmt = CONNECTION.prepareStatement(sql);
        stmt.setString(1, userId);
        ResultSet rs = stmt.executeQuery();

        List<FamilyGroup> groups = new ArrayList<>();
        while (rs.next()) {
            FamilyGroup group = new FamilyGroup();
            group.setGroupId(rs.getInt("group_id"));
            group.setGroupName(rs.getString("group_name"));
            group.setOwnerId(rs.getString("owner_id"));
            groups.add(group);
        }
        return groups;
    }

    public List<GroupMember> listMembersByGroup(int groupId) throws SQLException {
        String sql = "SELECT user_id, familyGroup_id FROM " + tabla + " WHERE familyGroup_id = ?";
        PreparedStatement stmt = CONNECTION.prepareStatement(sql);
        stmt.setInt(1, groupId);
        ResultSet rs = stmt.executeQuery();
        List<GroupMember> members = new ArrayList<>();
        while (rs.next()) {
            members.add(obtenerEntidad(rs));
        }
        return members;
    }

    /**
     * Sincroniza las librerías entre todos los miembros del grupo:
     * - Recopila todos los juegos comprados por cualquier miembro.
     * - Asegura que cada miembro tenga una entrada de library para cada juego comprado.
     * - No duplica entradas existentes.
     */
    public void syncLibrariesForGroup(int groupId) throws SQLException {
        // 1. Obtener todos los correos de los miembros del grupo
        String membersSql = "SELECT user_id FROM " + tabla + " WHERE familyGroup_id = ?";
        PreparedStatement mstmt = CONNECTION.prepareStatement(membersSql);
        mstmt.setInt(1, groupId);
        ResultSet mrs = mstmt.executeQuery();
        List<String> memberEmails = new ArrayList<>();
        while (mrs.next()) {
            memberEmails.add(mrs.getString("user_id"));
        }
        if (memberEmails.isEmpty()) return;

        // 2. Recopilar el conjunto de todos los game_id comprados por algún miembro
        List<Integer> allGameIds = new ArrayList<>();
        String buyedSql = "SELECT DISTINCT game_id FROM `library` WHERE user_id = ? AND buyed = TRUE";
        PreparedStatement bstmt = CONNECTION.prepareStatement(buyedSql);
        for (String email : memberEmails) {
            bstmt.setString(1, email);
            ResultSet brs = bstmt.executeQuery();
            while (brs.next()) {
                int gid = brs.getInt("game_id");
                if (!allGameIds.contains(gid)) {
                    allGameIds.add(gid);
                }
            }
        }

        if (allGameIds.isEmpty()) return;

        // 3. Para cada miembro, asegurar entrada de library para cada juego
        // Verificar si el miembro ya compró el juego (buyed=true) o si es prestado del grupo
        String existsSql = "SELECT buyed FROM `library` WHERE user_id = ? AND game_id = ?";
        PreparedStatement xstmt = CONNECTION.prepareStatement(existsSql);
        String insertSql = "INSERT INTO `library` (user_id, game_id, buyed, installed) VALUES (?,?,?,FALSE)";
        PreparedStatement istmt = CONNECTION.prepareStatement(insertSql);

        for (String email : memberEmails) {
            for (Integer gid : allGameIds) {
                xstmt.setString(1, email);
                xstmt.setInt(2, gid);
                ResultSet xrs = xstmt.executeQuery();
                if (!xrs.next()) {
                    // No existe entrada, verificar si este usuario compró el juego
                    String checkBuyerSql = "SELECT 1 FROM `library` WHERE user_id = ? AND game_id = ? AND buyed = TRUE";
                    PreparedStatement checkStmt = CONNECTION.prepareStatement(checkBuyerSql);
                    checkStmt.setString(1, email);
                    checkStmt.setInt(2, gid);
                    ResultSet checkRs = checkStmt.executeQuery();
                    boolean isBuyer = checkRs.next();
                    
                    // Insertar como prestado (buyed=false) para miembros que no compraron
                    istmt.setString(1, email);
                    istmt.setInt(2, gid);
                    istmt.setBoolean(3, isBuyer);
                    istmt.executeUpdate();
                }
                // Si ya existe, mantener el estado actual (no modificar buyed)
            }
        }
    }

    @Override
    public GroupMember obtenerEntidad(ResultSet rs) throws SQLException {
        GroupMember member = new GroupMember();
        
        member.setFamilyGroupId(rs.getInt("familyGroup_id"));
        member.setUserId(rs.getString("user_id"));
        
        return member;
    }
    
}
