package com.backend.categories;

public class GameCategoriesUpdateResult {
    private final int deleted;
    private final int inserted;

    public GameCategoriesUpdateResult(int deleted, int inserted) {
        this.deleted = deleted;
        this.inserted = inserted;
    }

    public int getDeleted() {
        return deleted;
    }

    public int getInserted() {
        return inserted;
    }
}
