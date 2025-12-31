package com.backend.entities;

import java.sql.Timestamp;

public class Invitation {
    private int invitationId;
    private String userId;
    private int familyGroupId;
    private String status;
    private Timestamp createdAt;

    public int getInvitationId() {
        return invitationId;
    }

    public void setInvitationId(int invitationId) {
        this.invitationId = invitationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getFamilyGroupId() {
        return familyGroupId;
    }

    public void setFamilyGroupId(int familyGroupId) {
        this.familyGroupId = familyGroupId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
