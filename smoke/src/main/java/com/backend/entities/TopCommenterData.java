package com.backend.entities;

/**
 * Entidad que representa un usuario comentarista para reportes.
 */
public class TopCommenterData {
    private String userId;
    private Integer commentsCount;

    public TopCommenterData() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(Integer commentsCount) {
        this.commentsCount = commentsCount;
    }
}
