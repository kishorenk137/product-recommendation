package com.analytics.customerinsights.service;

import com.analytics.customerinsights.dto.CategorySalesDto;
import com.analytics.customerinsights.dto.CustomerSegmentDto;
import com.analytics.customerinsights.dto.CustomerSummaryDto;
import com.analytics.customerinsights.dto.TopProductDto;
import com.analytics.customerinsights.entity.Order;
import com.analytics.customerinsights.repository.CustomerRepository;
import com.analytics.customerinsights.repository.OrderItemRepository;
import com.analytics.customerinsights.repository.OrderRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CustomerService customerService;
    private final SegmentationService segmentationService;

    public ReportService(OrderRepository orderRepository,
                         OrderItemRepository orderItemRepository,
                         CustomerService customerService,
                         SegmentationService segmentationService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.customerService = customerService;
        this.segmentationService = segmentationService;
    }

    public List<Order> getSalesReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = (startDate != null) ? startDate.atStartOfDay() : LocalDateTime.now().minusYears(3);
        LocalDateTime end = (endDate != null) ? endDate.atTime(LocalTime.MAX) : LocalDateTime.now();
        return orderRepository.findOrdersBetween(start, end);
    }

    public List<CustomerSummaryDto> getCustomerPurchaseReport() {
        return customerService.getAllCustomers().stream()
                .map(customerService::mapToSummaryDto)
                .sorted(Comparator.comparing(CustomerSummaryDto::getTotalSpent).reversed())
                .collect(Collectors.toList());
    }

    public List<TopProductDto> getProductPopularityReport() {
        List<Object[]> rows = orderItemRepository.findTopSellingProducts(PageRequest.of(0, 100));
        List<TopProductDto> list = new ArrayList<>();
        for (Object[] r : rows) {
            list.add(new TopProductDto(
                    ((Number) r[0]).longValue(),
                    (String) r[1],
                    (String) r[2],
                    ((Number) r[3]).longValue(),
                    (BigDecimal) r[4]
            ));
        }
        return list;
    }

    public List<CategorySalesDto> getCustomerPreferenceReport() {
        List<Object[]> rows = orderItemRepository.findSalesByCategory();
        List<CategorySalesDto> list = new ArrayList<>();
        for (Object[] r : rows) {
            list.add(new CategorySalesDto(
                    (String) r[0],
                    (BigDecimal) r[1],
                    ((Number) r[2]).longValue(),
                    ((Number) r[3]).longValue()
            ));
        }
        return list;
    }

    public List<CustomerSegmentDto> getCustomerSegmentationReport() {
        return segmentationService.getCustomerSegments();
    }

    // CSV Export Generators
    public String exportSalesCsv(LocalDate startDate, LocalDate endDate) {
        List<Order> orders = getSalesReport(startDate, endDate);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        pw.println("Order ID,Customer Name,Customer Email,Order Date,Status,Payment Method,Total Amount ($)");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (Order o : orders) {
            pw.printf("%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%.2f%n",
                    o.getId(),
                    escapeCsv(o.getCustomer().getName()),
                    escapeCsv(o.getCustomer().getEmail()),
                    o.getOrderDate().format(dtf),
                    o.getStatus(),
                    escapeCsv(o.getPaymentMethod()),
                    o.getTotalAmount());
        }
        return sw.toString();
    }

    public String exportCustomerPurchaseCsv() {
        List<CustomerSummaryDto> list = getCustomerPurchaseReport();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        pw.println("Customer ID,Name,Email,City,Registration Date,Total Orders,Total Spent ($),Preferred Category,Segment");
        for (CustomerSummaryDto c : list) {
            pw.printf("%d,\"%s\",\"%s\",\"%s\",%s,%d,%.2f,\"%s\",\"%s\"%n",
                    c.getId(),
                    escapeCsv(c.getName()),
                    escapeCsv(c.getEmail()),
                    escapeCsv(c.getCity()),
                    c.getRegistrationDate(),
                    c.getTotalPurchases(),
                    c.getTotalSpent(),
                    escapeCsv(c.getPreferredCategory()),
                    escapeCsv(c.getSegment()));
        }
        return sw.toString();
    }

    public String exportProductPopularityCsv() {
        List<TopProductDto> list = getProductPopularityReport();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        pw.println("Product ID,Product Name,Category,Units Sold,Total Revenue ($)");
        for (TopProductDto p : list) {
            pw.printf("%d,\"%s\",\"%s\",%d,%.2f%n",
                    p.getProductId(),
                    escapeCsv(p.getProductName()),
                    escapeCsv(p.getCategory()),
                    p.getQuantitySold(),
                    p.getTotalRevenue());
        }
        return sw.toString();
    }

    public String exportPreferenceCsv() {
        List<CategorySalesDto> list = getCustomerPreferenceReport();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        pw.println("Category,Total Orders,Total Units Sold,Total Revenue ($)");
        for (CategorySalesDto c : list) {
            pw.printf("\"%s\",%d,%d,%.2f%n",
                    escapeCsv(c.getCategory()),
                    c.getOrderCount(),
                    c.getQuantitySold(),
                    c.getTotalRevenue());
        }
        return sw.toString();
    }

    public String exportSegmentationCsv() {
        List<CustomerSegmentDto> segments = getCustomerSegmentationReport();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        pw.println("Segment Name,Criteria,Customer Count,Average Spend ($),Total Segment Revenue ($)");
        for (CustomerSegmentDto s : segments) {
            pw.printf("\"%s\",\"%s\",%d,%.2f,%.2f%n",
                    escapeCsv(s.getSegmentName()),
                    escapeCsv(s.getCriteria()),
                    s.getCustomerCount(),
                    s.getAverageSpend(),
                    s.getTotalRevenue());
        }
        return sw.toString();
    }

    private String escapeCsv(String val) {
        if (val == null) return "";
        return val.replace("\"", "\"\"");
    }
}
