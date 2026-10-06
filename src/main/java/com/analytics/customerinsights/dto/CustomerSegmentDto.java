package com.analytics.customerinsights.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CustomerSegmentDto {
    private String segmentName;
    private String description;
    private String criteria;
    private String badgeClass;
    private long customerCount;
    private BigDecimal totalRevenue;
    private BigDecimal averageSpend;
    private List<CustomerSummaryDto> customers = new ArrayList<>();

    public CustomerSegmentDto() {
        this.totalRevenue = BigDecimal.ZERO;
        this.averageSpend = BigDecimal.ZERO;
    }

    public CustomerSegmentDto(String segmentName, String description, String criteria, String badgeClass) {
        this.segmentName = segmentName;
        this.description = description;
        this.criteria = criteria;
        this.badgeClass = badgeClass;
        this.totalRevenue = BigDecimal.ZERO;
        this.averageSpend = BigDecimal.ZERO;
    }

    public String getSegmentName() {
        return segmentName;
    }

    public void setSegmentName(String segmentName) {
        this.segmentName = segmentName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCriteria() {
        return criteria;
    }

    public void setCriteria(String criteria) {
        this.criteria = criteria;
    }

    public String getBadgeClass() {
        return badgeClass;
    }

    public void setBadgeClass(String badgeClass) {
        this.badgeClass = badgeClass;
    }

    public long getCustomerCount() {
        return customerCount;
    }

    public void setCustomerCount(long customerCount) {
        this.customerCount = customerCount;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public BigDecimal getAverageSpend() {
        return averageSpend;
    }

    public void setAverageSpend(BigDecimal averageSpend) {
        this.averageSpend = averageSpend;
    }

    public List<CustomerSummaryDto> getCustomers() {
        return customers;
    }

    public void setCustomers(List<CustomerSummaryDto> customers) {
        this.customers = customers;
    }
}
