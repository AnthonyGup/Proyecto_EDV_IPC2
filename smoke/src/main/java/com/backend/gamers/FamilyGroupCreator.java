package com.backend.gamers;

import com.backend.daos.FamilyGroupDao;
import com.backend.daos.GroupMemberDao;
import com.backend.entities.FamilyGroup;
import com.backend.entities.Gamer;
import com.backend.entities.GroupMember;
import com.backend.exceptions.AlreadyExistException;
import java.sql.SQLException;

/**
 *
 * @author antho
 */
public class FamilyGroupCreator {
    
    public FamilyGroup createFamilyGroup(String groupName, Gamer owner) throws SQLException, AlreadyExistException {
        FamilyGroupDao fgDao = new FamilyGroupDao("familyGroup", "group_id");
        GroupMemberDao gmDao = new GroupMemberDao("groupMember", "");
        
        FamilyGroup group = new FamilyGroup();
        group.setGroupName(groupName);
        group.setOwnerId(owner.getMail());
        
        fgDao.create(group);
        
        FamilyGroup createdGroup = fgDao.readByColumn(groupName, "group_name");
        
        GroupMember ownerMember = new GroupMember();
        ownerMember.setFamilyGroupId(createdGroup.getGroupId());
        ownerMember.setUserId(owner.getMail());
        
        gmDao.create(ownerMember);
        
        return createdGroup;
    }
    
}
