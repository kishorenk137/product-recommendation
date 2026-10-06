package com.analytics.customerinsights.service;

import com.analytics.customerinsights.dto.CustomerSegmentDto;
import com.analytics.customerinsights.dto.CustomerSummaryDto;
import com.analytics.customerinsights.entity.Customer;
import com.analytics.customerinsights.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class SegmentationService {

    private final CustomerRepository customerRepository;
    private final CustomerService customerService;

    // Configurable thresholds with sensible business defaults
    @Value("${app.segmentation.high-spending-threshold:1000.00}")
    private double highSpendingThreshold = 1000.00;

    @Value("${app.segmentation.frequent-orders-threshold:5}")
    private int frequentOrdersThreshold = 5;

    @Value("${app.segmentation.new-customer-days:60}")
    private int newCustomerDays = 60;

    public SegmentationService(CustomerRepository customerRepository, CustomerService customerService) {
        this.customerRepository = customerRepository;
        this.customerService = customerService;
    }

    public List<CustomerSegmentDto> getCustomerSegments() {
        List<Customer> allCustomers = customerRepository.findAll();
        List<CustomerSummaryDto> summaryList = allCustomers.stream()
                .map(customerService::mapToSummaryDto)
                .collect(Collectors.toList());

        CustomerSegmentDto highSpenders = new CustomerSegmentDto(
                "High-Spending Customers",
                "VIP shoppers with substantial cumulative purchase revenue exceeding the threshold.",
                String.format("Total spending >= $%.2f", highSpendingThreshold),
                "bg-success"
        );

        CustomerSegmentDto frequentBuyers = new CustomerSegmentDto(
                "Frequent Buyers",
                "Loyal and recurring customers with consistent, repeat transaction volume.",
                String.format("Total orders >= %d and spending < $%.2f", frequentOrdersThreshold, highSpendingThreshold),
                "bg-primary"
        );

        CustomerSegmentDto occasionalBuyers = new CustomerSegmentDto(
                "Occasional Buyers",
                "Periodic shoppers who purchase occasionally (2 to 4 completed orders).",
                "Total orders between 2 and 4",
                "bg-info text-dark"
        );

        CustomerSegmentDto newCustomers = new CustomerSegmentDto(
                "New Customers",
                "Recently acquired customers registered recently who are just getting started.",
                String.format("Registered within %d days with <= 1 order", newCustomerDays),
                "bg-warning text-dark"
        );

        CustomerSegmentDto inactiveCustomers = new CustomerSegmentDto(
                "Inactive Customers",
                "Customers with zero orders after initial onboarding or dormant accounts.",
                String.format("Registered > %d days ago with 0 orders", newCustomerDays),
                "bg-secondary"
        );

        Map<String, CustomerSegmentDto> segmentMap = new LinkedHashMap<>();
        segmentMap.put("High-Spending Customer", highSpenders);
        segmentMap.put("Frequent Buyer", frequentBuyers);
        segmentMap.put("Occasional Buyer", occasionalBuyers);
        segmentMap.put("New Customer", newCustomers);
        segmentMap.put("Inactive Customer", inactiveCustomers);

        for (CustomerSummaryDto c : summaryList) {
            CustomerSegmentDto seg = segmentMap.get(c.getSegment());
            if (seg != null) {
                seg.getCustomers().add(c);
                seg.setTotalRevenue(seg.getTotalRevenue().add(c.getTotalSpent()));
            }
        }

        // Compute counts and average spend for each segment
        for (CustomerSegmentDto seg : segmentMap.values()) {
            seg.setCustomerCount(seg.getCustomers().size());
            if (seg.getCustomerCount() > 0) {
                seg.setAverageSpend(seg.getTotalRevenue().divide(BigDecimal.valueOf(seg.getCustomerCount()), 2, RoundingMode.HALF_UP));
            }
        }

        return new ArrayList<>(segmentMap.values());
    }

    public double getHighSpendingThreshold() {
        return highSpendingThreshold;
    }

    public int getFrequentOrdersThreshold() {
        return frequentOrdersThreshold;
    }

    public int getNewCustomerDays() {
        return newCustomerDays;
    }
}
