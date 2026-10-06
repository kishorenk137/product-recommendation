package com.analytics.customerinsights.dto;

import java.math.BigDecimal;

public class TopProductDto {
    private Long productId;
    private String productName;
    private String category;
    private long quantitySold;
    private BigDecimal totalRevenue;

    public TopProductDto() {
        this.totalRevenue = BigDecimal.ZERO;
    }

    public TopProductDto(Long productId, String productName, String category, long quantitySold, BigDecimal totalRevenue) {
        this.productId = productId;
        this.productName = productName;
        this.category = category;
        this.quantitySold = quantitySold;
        this.totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public long getQuantitySold() {
        return quantitySold;
    }

    public void setQuantitySold(long quantitySold) {
        this.quantitySold = quantitySold;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
}
