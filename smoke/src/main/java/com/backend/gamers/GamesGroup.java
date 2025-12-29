/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.backend.gamers;

import com.backend.daos.GroupMemberDao;
import com.backend.daos.LibraryDao;
import com.backend.entities.FamilyGroup;
import com.backend.entities.Gamer;
import com.backend.entities.GroupMember;
import com.backend.entities.Library;
import com.backend.exceptions.AlreadyExistException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author antho
 */
public class GamesGroup {
    
    private Gamer owner;
    private FamilyGroup group;
    
    public GamesGroup(Gamer owner, FamilyGroup group) {
        this.owner = owner;
        this.group = group;
    }
    
    public void addGamesToGamer(Gamer targetGamer) throws SQLException, AlreadyExistException {
        GroupMemberDao groupDao = new GroupMemberDao("groupMember", "");
        LibraryDao libDao = new LibraryDao("`library`", "");
        
            // obtener todos los miembros del grupo
            List<GroupMember> members = groupDao.readAll();
            List<String> memberIds = new ArrayList<>();
            for (GroupMember m : members) {
                if (m.getFamilyGroupId() == group.getGroupId()) {
                    if (!memberIds.contains(m.getUserId())) {
                        memberIds.add(m.getUserId());
                    }
                }
            }
            
            // obtener todos los registros de juegos comprados  
            List<Integer> allGameIds = new ArrayList<>();
            for (String memberId : memberIds) {
                List<Library> libs = libDao.readByGamer(memberId);
                for (Library lib : libs) {
                    if (lib.isBuyed() && !allGameIds.contains(lib.getGameId())) {
                        allGameIds.add(lib.getGameId());
                    }
                }
            }
            
            // obtener la library del gamer objetivo
            List<Library> targetLibs = libDao.readByGamer(targetGamer.getMail());
            List<Integer> targetGameIds = new ArrayList<>();
            for (Library lib : targetLibs) {
                targetGameIds.add(lib.getGameId());
            }
            
            // añadir los juegos que no tenga
            for (Integer gameId : allGameIds) {
                if (!targetGameIds.contains(gameId)) {
                    Library newLib = new Library();
                    newLib.setGamer_id(targetGamer.getMail());
                    newLib.setGameId(gameId);
                    newLib.setBuyed(false);
                    newLib.setInstalled(true);
                    libDao.create(newLib);
                }
            }
    }
    
}
