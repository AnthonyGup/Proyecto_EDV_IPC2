package com.backend.daos;

import com.backend.crud.Crud;
import com.backend.entities.Invitation;
import com.backend.exceptions.AlreadyExistException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class InvitationDao extends Crud<Invitation> {

    public InvitationDao(String tabla, String codigo) {
        super(tabla, codigo);
    }

    @Override
    public void create(Invitation entidad) throws SQLException, AlreadyExistException {
        if (hasPending(entidad.getUserId(), entidad.getFamilyGroupId())) {
            throw new AlreadyExistException();
        }

        String sql = "INSERT INTO " + tabla + " (user_id, familyGroup_id, status) VALUES (?,?,?)";
        PreparedStatement stmt = CONNECTION.prepareStatement(sql);
        stmt.setString(1, entidad.getUserId());
        stmt.setInt(2, entidad.getFamilyGroupId());
        stmt.setString(3, entidad.getStatus() == null ? "PENDING" : entidad.getStatus());
        stmt.executeUpdate();
    }

    public boolean hasPending(String userId, int groupId) throws SQLException {
        String sql = "SELECT 1 FROM " + tabla + " WHERE user_id = ? AND familyGroup_id = ? AND status = 'PENDING'";
        PreparedStatement stmt = CONNECTION.prepareStatement(sql);
        stmt.setString(1, userId);
        stmt.setInt(2, groupId);
        ResultSet rs = stmt.executeQuery();
        return rs.next();
    }

    public List<Invitation> listByUser(String userId) throws SQLException {
        String sql = "SELECT * FROM " + tabla + " WHERE user_id = ? ORDER BY created_at DESC";
        PreparedStatement stmt = CONNECTION.prepareStatement(sql);
        stmt.setString(1, userId);
        ResultSet rs = stmt.executeQuery();
        List<Invitation> invites = new ArrayList<>();
        while (rs.next()) {
            invites.add(obtenerEntidad(rs));
        }
        return invites;
    }

    @Override
    public Invitation obtenerEntidad(ResultSet rs) throws SQLException {
        Invitation invitation = new Invitation();
        invitation.setInvitationId(rs.getInt("invitation_id"));
        invitation.setUserId(rs.getString("user_id"));
        invitation.setFamilyGroupId(rs.getInt("familyGroup_id"));
        invitation.setStatus(rs.getString("status"));
        invitation.setCreatedAt(rs.getTimestamp("created_at"));
        return invitation;
    }
}
