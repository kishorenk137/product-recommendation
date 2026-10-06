package com.analytics.customerinsights.dto;

import java.math.BigDecimal;

public class CategorySalesDto {
    private String category;
    private BigDecimal totalRevenue;
    private long orderCount;
    private long quantitySold;
    private double percentage;

    public CategorySalesDto() {
        this.totalRevenue = BigDecimal.ZERO;
    }

    public CategorySalesDto(String category, BigDecimal totalRevenue, long orderCount, long quantitySold) {
        this.category = category;
        this.totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
        this.orderCount = orderCount;
        this.quantitySold = quantitySold;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public long getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(long orderCount) {
        this.orderCount = orderCount;
    }

    public long getQuantitySold() {
        return quantitySold;
    }

    public void setQuantitySold(long quantitySold) {
        this.quantitySold = quantitySold;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }
}
