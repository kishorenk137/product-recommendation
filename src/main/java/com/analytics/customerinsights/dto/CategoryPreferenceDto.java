package com.analytics.customerinsights.dto;

import java.math.BigDecimal;

public class CategoryPreferenceDto {
    private String category;
    private long purchaseCount;
    private BigDecimal totalSpent;
    private double frequencyShare;
    private double spendShare;
    private double affinityScore;
    private double recencyScore;
    private Long daysSinceLastPurchase;

    public CategoryPreferenceDto() {
        this.totalSpent = BigDecimal.ZERO;
    }

    public CategoryPreferenceDto(String category, long purchaseCount, BigDecimal totalSpent) {
        this.category = category;
        this.purchaseCount = purchaseCount;
        this.totalSpent = totalSpent != null ? totalSpent : BigDecimal.ZERO;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public long getPurchaseCount() {
        return purchaseCount;
    }

    public void setPurchaseCount(long purchaseCount) {
        this.purchaseCount = purchaseCount;
    }

    public BigDecimal getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(BigDecimal totalSpent) {
        this.totalSpent = totalSpent;
    }

    public double getFrequencyShare() {
        return frequencyShare;
    }

    public void setFrequencyShare(double frequencyShare) {
        this.frequencyShare = frequencyShare;
    }

    public double getSpendShare() {
        return spendShare;
    }

    public void setSpendShare(double spendShare) {
        this.spendShare = spendShare;
    }

    public double getAffinityScore() {
        return affinityScore;
    }

    public void setAffinityScore(double affinityScore) {
        this.affinityScore = affinityScore;
    }

    public double getRecencyScore() {
        return recencyScore;
    }

    public void setRecencyScore(double recencyScore) {
        this.recencyScore = recencyScore;
    }

    public Long getDaysSinceLastPurchase() {
        return daysSinceLastPurchase;
    }

    public void setDaysSinceLastPurchase(Long daysSinceLastPurchase) {
        this.daysSinceLastPurchase = daysSinceLastPurchase;
    }
}
