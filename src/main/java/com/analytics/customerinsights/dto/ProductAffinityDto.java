package com.analytics.customerinsights.dto;

public class ProductAffinityDto {
    private String productA;
    private String productB;
    private String categoryA;
    private String categoryB;
    private long coOccurrenceCount;
    private double confidencePercentage;
    private String insight;

    public ProductAffinityDto() {
    }

    public ProductAffinityDto(String productA, String productB, String categoryA, String categoryB,
                              long coOccurrenceCount, double confidencePercentage, String insight) {
        this.productA = productA;
        this.productB = productB;
        this.categoryA = categoryA;
        this.categoryB = categoryB;
        this.coOccurrenceCount = coOccurrenceCount;
        this.confidencePercentage = confidencePercentage;
        this.insight = insight;
    }

    public String getProductA() {
        return productA;
    }

    public void setProductA(String productA) {
        this.productA = productA;
    }

    public String getProductB() {
        return productB;
    }

    public void setProductB(String productB) {
        this.productB = productB;
    }

    public String getCategoryA() {
        return categoryA;
    }

    public void setCategoryA(String categoryA) {
        this.categoryA = categoryA;
    }

    public String getCategoryB() {
        return categoryB;
    }

    public void setCategoryB(String categoryB) {
        this.categoryB = categoryB;
    }

    public long getCoOccurrenceCount() {
        return coOccurrenceCount;
    }

    public void setCoOccurrenceCount(long coOccurrenceCount) {
        this.coOccurrenceCount = coOccurrenceCount;
    }

    public double getConfidencePercentage() {
        return confidencePercentage;
    }

    public void setConfidencePercentage(double confidencePercentage) {
        this.confidencePercentage = confidencePercentage;
    }

    public String getInsight() {
        return insight;
    }

    public void setInsight(String insight) {
        this.insight = insight;
    }
}
