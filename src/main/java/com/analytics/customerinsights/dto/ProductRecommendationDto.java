package com.analytics.customerinsights.dto;

import java.math.BigDecimal;

public class ProductRecommendationDto {
    private Long productId;
    private String productName;
    private String category;
    private BigDecimal price;
    private Integer stockQuantity;
    private double confidenceScore;
    private String recommendationReason;
    private String matchType; // e.g. "Direct Affinity", "Category Trend", "Popular Choice"

    public ProductRecommendationDto() {
        this.price = BigDecimal.ZERO;
    }

    public ProductRecommendationDto(Long productId, String productName, String category, BigDecimal price,
                                  Integer stockQuantity, double confidenceScore, String recommendationReason, String matchType) {
        this.productId = productId;
        this.productName = productName;
        this.category = category;
        this.price = price != null ? price : BigDecimal.ZERO;
        this.stockQuantity = stockQuantity != null ? stockQuantity : 0;
        this.confidenceScore = confidenceScore;
        this.recommendationReason = recommendationReason;
        this.matchType = matchType;
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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public String getRecommendationReason() {
        return recommendationReason;
    }

    public void setRecommendationReason(String recommendationReason) {
        this.recommendationReason = recommendationReason;
    }

    public String getMatchType() {
        return matchType;
    }

    public void setMatchType(String matchType) {
        this.matchType = matchType;
    }
}
