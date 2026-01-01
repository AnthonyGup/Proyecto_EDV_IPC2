package com.backend.entities;

/**
 * Entidad que representa los datos de ganancias de una empresa para reportes.
 */
public class CompanyEarningsData {
    private String companyName;
    private Integer salesCount;
    private Double totalSales;
    private Double commissionPercentage;
    private Double commissionRetained;
    private Double companyEarnings;

    public CompanyEarningsData() {
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public Integer getSalesCount() {
        return salesCount;
    }

    public void setSalesCount(Integer salesCount) {
        this.salesCount = salesCount;
    }

    public Double getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(Double totalSales) {
        this.totalSales = totalSales;
    }

    public Double getCommissionPercentage() {
        return commissionPercentage;
    }

    public void setCommissionPercentage(Double commissionPercentage) {
        this.commissionPercentage = commissionPercentage;
    }

    public Double getCommissionRetained() {
        return commissionRetained;
    }

    public void setCommissionRetained(Double commissionRetained) {
        this.commissionRetained = commissionRetained;
    }

    public Double getCompanyEarnings() {
        return companyEarnings;
    }

    public void setCompanyEarnings(Double companyEarnings) {
        this.companyEarnings = companyEarnings;
    }
}
