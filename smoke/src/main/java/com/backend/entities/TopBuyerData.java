package com.backend.entities;

/**
 * Entidad que representa un usuario comprador para reportes.
 */
public class TopBuyerData {
    private String userId;
    private Integer purchasesCount;
    private Double totalSpent;
    private Integer uniqueGames;

    public TopBuyerData() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getPurchasesCount() {
        return purchasesCount;
    }

    public void setPurchasesCount(Integer purchasesCount) {
        this.purchasesCount = purchasesCount;
    }

    public Double getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(Double totalSpent) {
        this.totalSpent = totalSpent;
    }

    public Integer getUniqueGames() {
        return uniqueGames;
    }

    public void setUniqueGames(Integer uniqueGames) {
        this.uniqueGames = uniqueGames;
    }
}
