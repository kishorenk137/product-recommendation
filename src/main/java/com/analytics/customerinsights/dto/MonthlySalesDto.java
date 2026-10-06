package com.analytics.customerinsights.dto;

import java.math.BigDecimal;

public class MonthlySalesDto {
    private String monthLabel;
    private BigDecimal revenue;
    private long orderCount;

    public MonthlySalesDto() {
        this.revenue = BigDecimal.ZERO;
    }

    public MonthlySalesDto(String monthLabel, BigDecimal revenue, long orderCount) {
        this.monthLabel = monthLabel;
        this.revenue = revenue != null ? revenue : BigDecimal.ZERO;
        this.orderCount = orderCount;
    }

    public String getMonthLabel() {
        return monthLabel;
    }

    public void setMonthLabel(String monthLabel) {
        this.monthLabel = monthLabel;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }

    public long getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(long orderCount) {
        this.orderCount = orderCount;
    }
}
