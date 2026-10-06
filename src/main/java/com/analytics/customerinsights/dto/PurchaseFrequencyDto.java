package com.analytics.customerinsights.dto;

public class PurchaseFrequencyDto {
    private String rangeLabel;
    private long customerCount;
    private double percentage;

    public PurchaseFrequencyDto() {
    }

    public PurchaseFrequencyDto(String rangeLabel, long customerCount, double percentage) {
        this.rangeLabel = rangeLabel;
        this.customerCount = customerCount;
        this.percentage = percentage;
    }

    public String getRangeLabel() {
        return rangeLabel;
    }

    public void setRangeLabel(String rangeLabel) {
        this.rangeLabel = rangeLabel;
    }

    public long getCustomerCount() {
        return customerCount;
    }

    public void setCustomerCount(long customerCount) {
        this.customerCount = customerCount;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }
}
