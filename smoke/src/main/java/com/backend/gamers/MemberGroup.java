/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backend.gamers;

import com.backend.daos.GroupMemberDao;
import com.backend.entities.FamilyGroup;
import com.backend.entities.Gamer;
import com.backend.entities.GroupMember;
import com.backend.exceptions.AlreadyExistException;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author antho
 */
public class MemberGroup {
    
    private Gamer owner;
    private FamilyGroup group;
    
    public MemberGroup(Gamer owner, FamilyGroup group) {
        this.owner = owner;
        this.group = group;
    }
    
    public void addMember(String member_id) {
        if (haveSpace()) {
            GroupMember gm = new GroupMember();
            gm.setFamilyGroupId(group.getGroupId());
            gm.setUserId(member_id);
            
            GroupMemberDao dao = new GroupMemberDao("groupMember", "");
            try {
                dao.create(gm);
            } catch (SQLException | AlreadyExistException e) {
                e.printStackTrace();
            }
        }
    }
    
    private boolean haveSpace() {
        GroupMemberDao dao = new GroupMemberDao("groupMember", "");
        try {
            List<GroupMember> allMembers = dao.readAll();
            int count = 0;
            for (GroupMember m : allMembers) {
                if (m.getFamilyGroupId() == group.getGroupId()) {
                    count++;
                }
            }
            return count < 6;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
}
