package com.backend.gamers;

import com.backend.daos.FamilyGroupDao;
import com.backend.daos.GroupMemberDao;
import com.backend.daos.InvitationDao;
import com.backend.db.DBConnection;
import com.backend.entities.FamilyGroup;
import com.backend.entities.GroupMember;
import com.backend.entities.Invitation;
import com.backend.exceptions.AlreadyExistException;
import com.backend.exceptions.ServiceException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FamilyGroupService {

    private final FamilyGroupDao familyGroupDao = new FamilyGroupDao("familyGroup", "group_id");
    private final GroupMemberDao groupMemberDao = new GroupMemberDao("groupMember", "");
    private final InvitationDao invitationDao = new InvitationDao("invitation", "invitation_id");

    public List<FamilyGroup> listGroups(String userId, String ownerId) throws SQLException, ServiceException {
        if ((userId == null || userId.isBlank()) && (ownerId == null || ownerId.isBlank())) {
            throw new ServiceException(400, "Se requiere userId u ownerId");
        }

        List<FamilyGroup> groups = new ArrayList<>();
        if (userId != null && !userId.isBlank()) {
            groups.addAll(groupMemberDao.listGroupsByUser(userId));
        }
        if (ownerId != null && !ownerId.isBlank()) {
            List<FamilyGroup> owned = familyGroupDao.listByOwner(ownerId);
            for (FamilyGroup g : owned) {
                boolean exists = groups.stream().anyMatch(existing -> existing.getGroupId() == g.getGroupId());
                if (!exists) {
                    groups.add(g);
                }
            }
        }
        return groups;
    }

    public FamilyGroup createGroup(String groupName, String ownerId) throws SQLException, ServiceException, AlreadyExistException {
        if (groupName == null || groupName.isBlank() || ownerId == null || ownerId.isBlank()) {
            throw new ServiceException(400, "Parametros invalidos");
        }

        FamilyGroup familyGroup = new FamilyGroup();
        familyGroup.setGroupName(groupName);
        familyGroup.setOwnerId(ownerId);

        familyGroupDao.create(familyGroup);
        FamilyGroup created = familyGroupDao.readByColumn(groupName, "group_name");

        if (created != null) {
            GroupMember ownerMember = new GroupMember();
            ownerMember.setFamilyGroupId(created.getGroupId());
            ownerMember.setUserId(ownerId);
            try {
                groupMemberDao.create(ownerMember);
            } catch (AlreadyExistException ignored) {
                // Already member, ignore
            }
        }
        return created;
    }

    public void deleteGroup(int groupId, String requesterId) throws SQLException, ServiceException {
        FamilyGroup group = familyGroupDao.readByPk(String.valueOf(groupId));
        if (group == null) {
            throw new ServiceException(404, "Grupo no encontrado");
        }
        if (!group.getOwnerId().equals(requesterId)) {
            throw new ServiceException(403, "Solo el dueno puede eliminar el grupo");
        }

        List<GroupMember> members = groupMemberDao.listMembersByGroup(groupId);
        cleanupLibrariesForMembers(members, groupMemberDao);

        boolean deleted = familyGroupDao.delete(String.valueOf(groupId));
        if (!deleted) {
            throw new ServiceException(500, "No se pudo eliminar el grupo");
        }
    }

    public void inviteMember(int groupId, String inviteeEmail, String inviterEmail) throws SQLException, ServiceException, AlreadyExistException {
        if (inviteeEmail == null || inviteeEmail.isBlank() || inviterEmail == null || inviterEmail.isBlank()) {
            throw new ServiceException(400, "Datos incompletos");
        }
        FamilyGroup group = familyGroupDao.readByPk(String.valueOf(groupId));
        if (group == null) {
            throw new ServiceException(404, "Grupo no encontrado");
        }
        if (group.getOwnerId() == null || !group.getOwnerId().equals(inviterEmail)) {
            throw new ServiceException(403, "Solo el propietario del grupo puede invitar miembros");
        }
        if (groupMemberDao.isMember(inviteeEmail, groupId)) {
            throw new ServiceException(400, "El usuario ya es miembro del grupo");
        }

        Invitation invitation = new Invitation();
        invitation.setFamilyGroupId(groupId);
        invitation.setUserId(inviteeEmail);
        invitation.setStatus("PENDING");

        invitationDao.create(invitation);
    }

    public List<InvitationView> listInvitations(String userEmail) throws SQLException, ServiceException {
        if (userEmail == null || userEmail.isBlank()) {
            throw new ServiceException(400, "userEmail requerido");
        }

        String sql = "SELECT i.invitation_id, i.user_id, i.familyGroup_id, i.status, i.created_at, "
                + "fg.group_name, fg.owner_id "
                + "FROM invitation i JOIN familyGroup fg ON i.familyGroup_id = fg.group_id "
                + "WHERE i.user_id = ? AND i.status = 'PENDING' ORDER BY i.created_at DESC";
        PreparedStatement stmt = DBConnection.getInstance().getConnection().prepareStatement(sql);
        stmt.setString(1, userEmail);
        ResultSet rs = stmt.executeQuery();
        List<InvitationView> invitations = new ArrayList<>();
        while (rs.next()) {
            InvitationView view = new InvitationView();
            view.setInvitationId(rs.getInt("invitation_id"));
            view.setUserId(rs.getString("user_id"));
            view.setFamilyGroupId(rs.getInt("familyGroup_id"));
            view.setStatus(rs.getString("status"));
            view.setCreatedAt(rs.getTimestamp("created_at").toString());
            view.setGroupName(rs.getString("group_name"));
            view.setOwnerId(rs.getString("owner_id"));
            invitations.add(view);
        }
        return invitations;
    }

    public void respondInvitation(int invitationId, String action, String userEmail) throws SQLException, ServiceException, AlreadyExistException {
        if (action == null || action.isBlank() || userEmail == null || userEmail.isBlank()) {
            throw new ServiceException(400, "Datos incompletos");
        }
        String readSql = "SELECT invitation_id, user_id, familyGroup_id FROM invitation WHERE invitation_id = ?";
        PreparedStatement rstmt = DBConnection.getInstance().getConnection().prepareStatement(readSql);
        rstmt.setInt(1, invitationId);
        ResultSet rrs = rstmt.executeQuery();
        if (!rrs.next()) {
            throw new ServiceException(404, "Invitacion no encontrada");
        }
        String invitedUser = rrs.getString("user_id");
        int groupId = rrs.getInt("familyGroup_id");
        if (!invitedUser.equals(userEmail)) {
            throw new ServiceException(403, "No puedes gestionar esta invitacion");
        }

        if ("accept".equalsIgnoreCase(action)) {
            GroupMember gm = new GroupMember();
            gm.setFamilyGroupId(groupId);
            gm.setUserId(invitedUser);
            try {
                groupMemberDao.create(gm);
            } catch (AlreadyExistException ignored) {
                // Already member, ignore
            }
            invitationDao.delete(String.valueOf(invitationId));
        } else if ("reject".equalsIgnoreCase(action)) {
            invitationDao.delete(String.valueOf(invitationId));
        } else {
            throw new ServiceException(400, "Accion invalida");
        }
    }

    public List<GroupMember> listMembers(int groupId) throws SQLException, ServiceException {
        List<GroupMember> members = groupMemberDao.listMembersByGroup(groupId);
        if (members == null) {
            throw new ServiceException(404, "Grupo no encontrado");
        }
        return members;
    }

    private void cleanupLibrariesForMembers(List<GroupMember> members, GroupMemberDao gmDao) throws SQLException {
        Connection conn = DBConnection.getInstance().getConnection();

        for (GroupMember member : members) {
            String userId = member.getUserId();

            List<FamilyGroup> otherGroups = gmDao.listGroupsByUser(userId);
            otherGroups.removeIf(g -> g.getGroupId() == member.getFamilyGroupId());

            List<Integer> protectedGameIds = new ArrayList<>();
            if (!otherGroups.isEmpty()) {
                for (FamilyGroup otherGroup : otherGroups) {
                    List<GroupMember> otherMembers = gmDao.listMembersByGroup(otherGroup.getGroupId());
                    for (GroupMember om : otherMembers) {
                        String memberSql = "SELECT DISTINCT game_id FROM `library` WHERE user_id = ? AND buyed = TRUE";
                        PreparedStatement pstmt = conn.prepareStatement(memberSql);
                        pstmt.setString(1, om.getUserId());
                        ResultSet rs = pstmt.executeQuery();
                        while (rs.next()) {
                            int gid = rs.getInt("game_id");
                            if (!protectedGameIds.contains(gid)) {
                                protectedGameIds.add(gid);
                            }
                        }
                    }
                }
            }

            PreparedStatement dstmt;
            if (protectedGameIds.isEmpty()) {
                dstmt = conn.prepareStatement("DELETE FROM `library` WHERE user_id = ? AND buyed = FALSE");
                dstmt.setString(1, userId);
            } else {
                StringBuilder placeholders = new StringBuilder();
                for (int i = 0; i < protectedGameIds.size(); i++) {
                    if (i > 0) {
                        placeholders.append(",");
                    }
                    placeholders.append("?");
                }
                String deleteSql = "DELETE FROM `library` WHERE user_id = ? AND buyed = FALSE AND game_id NOT IN (" + placeholders + ")";
                dstmt = conn.prepareStatement(deleteSql);
                dstmt.setString(1, userId);
                for (int i = 0; i < protectedGameIds.size(); i++) {
                    dstmt.setInt(i + 2, protectedGameIds.get(i));
                }
            }
            dstmt.executeUpdate();
        }
    }

}
