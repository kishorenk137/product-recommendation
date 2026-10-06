package com.analytics.customerinsights.dto;

import java.math.BigDecimal;

public class DashboardStatsDto {
    private long totalCustomers;
    private long totalOrders;
    private BigDecimal totalRevenue;
    private BigDecimal averageOrderValue;
    private long newCustomersCount;
    private long returningCustomersCount;

    public DashboardStatsDto() {
        this.totalRevenue = BigDecimal.ZERO;
        this.averageOrderValue = BigDecimal.ZERO;
    }

    public long getTotalCustomers() {
        return totalCustomers;
    }

    public void setTotalCustomers(long totalCustomers) {
        this.totalCustomers = totalCustomers;
    }

    public long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public BigDecimal getAverageOrderValue() {
        return averageOrderValue;
    }

    public void setAverageOrderValue(BigDecimal averageOrderValue) {
        this.averageOrderValue = averageOrderValue;
    }

    public long getNewCustomersCount() {
        return newCustomersCount;
    }

    public void setNewCustomersCount(long newCustomersCount) {
        this.newCustomersCount = newCustomersCount;
    }

    public long getReturningCustomersCount() {
        return returningCustomersCount;
    }

    public void setReturningCustomersCount(long returningCustomersCount) {
        this.returningCustomersCount = returningCustomersCount;
    }
}
