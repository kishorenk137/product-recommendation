package com.analytics.customerinsights.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CustomerPreferencePredictionDto {
    private Long customerId;
    private String customerName;
    private String email;
    private int totalOrders;
    private BigDecimal totalSpent;
    private boolean hasSufficientData;
    private String topPredictedCategory;
    private double predictionConfidence;
    private List<CategoryPreferenceDto> categoryPreferences = new ArrayList<>();
    private List<String> predictionReasons = new ArrayList<>();

    public CustomerPreferencePredictionDto() {
        this.totalSpent = BigDecimal.ZERO;
        this.hasSufficientData = true;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(int totalOrders) {
        this.totalOrders = totalOrders;
    }

    public BigDecimal getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(BigDecimal totalSpent) {
        this.totalSpent = totalSpent;
    }

    public boolean isHasSufficientData() {
        return hasSufficientData;
    }

    public void setHasSufficientData(boolean hasSufficientData) {
        this.hasSufficientData = hasSufficientData;
    }

    public String getTopPredictedCategory() {
        return topPredictedCategory;
    }

    public void setTopPredictedCategory(String topPredictedCategory) {
        this.topPredictedCategory = topPredictedCategory;
    }

    public double getPredictionConfidence() {
        return predictionConfidence;
    }

    public void setPredictionConfidence(double predictionConfidence) {
        this.predictionConfidence = predictionConfidence;
    }

    public List<CategoryPreferenceDto> getCategoryPreferences() {
        return categoryPreferences;
    }

    public void setCategoryPreferences(List<CategoryPreferenceDto> categoryPreferences) {
        this.categoryPreferences = categoryPreferences;
    }

    public List<String> getPredictionReasons() {
        return predictionReasons;
    }

    public void setPredictionReasons(List<String> predictionReasons) {
        this.predictionReasons = predictionReasons;
    }
}
