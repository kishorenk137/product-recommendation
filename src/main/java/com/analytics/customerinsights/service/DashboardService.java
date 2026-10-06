package com.analytics.customerinsights.service;

import com.analytics.customerinsights.dto.*;
import com.analytics.customerinsights.entity.Order;
import com.analytics.customerinsights.repository.CustomerRepository;
import com.analytics.customerinsights.repository.OrderItemRepository;
import com.analytics.customerinsights.repository.OrderRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public DashboardService(CustomerRepository customerRepository,
                            OrderRepository orderRepository,
                            OrderItemRepository orderItemRepository) {
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    public DashboardStatsDto getDashboardStats() {
        DashboardStatsDto stats = new DashboardStatsDto();

        long totalCustomers = customerRepository.count();
        long totalOrders = orderRepository.count();
        BigDecimal totalRevenue = orderRepository.calculateTotalRevenue();
        if (totalRevenue == null) {
            totalRevenue = BigDecimal.ZERO;
        }

        BigDecimal aov = BigDecimal.ZERO;
        if (totalOrders > 0) {
            aov = totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP);
        }

        // New customers in last 30 days
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        long newCustomers = customerRepository.countByRegistrationDateAfter(thirtyDaysAgo);
        long returningCustomers = Math.max(0, totalCustomers - newCustomers);

        stats.setTotalCustomers(totalCustomers);
        stats.setTotalOrders(totalOrders);
        stats.setTotalRevenue(totalRevenue);
        stats.setAverageOrderValue(aov);
        stats.setNewCustomersCount(newCustomers);
        stats.setReturningCustomersCount(returningCustomers);

        return stats;
    }

    public List<MonthlySalesDto> getMonthlySalesData() {
        // Retrieve orders over the last 6 months
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6).withDayOfMonth(1).withHour(0).withMinute(0);
        List<Order> orders = orderRepository.findOrdersSince(sixMonthsAgo);

        // Group by Year-Month
        DateTimeFormatter keyFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
        DateTimeFormatter labelFormatter = DateTimeFormatter.ofPattern("MMM yyyy");

        // Pre-populate last 6 calendar months in chronological order
        Map<String, MonthlySalesDto> monthMap = new LinkedHashMap<>();
        for (int i = 5; i >= 0; i--) {
            LocalDateTime monthDate = LocalDateTime.now().minusMonths(i);
            String key = monthDate.format(keyFormatter);
            String label = monthDate.format(labelFormatter);
            monthMap.put(key, new MonthlySalesDto(label, BigDecimal.ZERO, 0));
        }

        for (Order order : orders) {
            if ("COMPLETED".equalsIgnoreCase(order.getStatus())) {
                String key = order.getOrderDate().format(keyFormatter);
                MonthlySalesDto dto = monthMap.get(key);
                if (dto != null) {
                    dto.setRevenue(dto.getRevenue().add(order.getTotalAmount()));
                    dto.setOrderCount(dto.getOrderCount() + 1);
                }
            }
        }

        return new ArrayList<>(monthMap.values());
    }

    public List<CategorySalesDto> getCategorySalesDistribution() {
        List<Object[]> rawList = orderItemRepository.findSalesByCategory();
        BigDecimal totalAllRevenue = BigDecimal.ZERO;

        List<CategorySalesDto> dtos = new ArrayList<>();
        for (Object[] row : rawList) {
            String category = (String) row[0];
            BigDecimal revenue = (BigDecimal) row[1];
            long orderCount = ((Number) row[2]).longValue();
            long quantity = ((Number) row[3]).longValue();

            dtos.add(new CategorySalesDto(category, revenue, orderCount, quantity));
            totalAllRevenue = totalAllRevenue.add(revenue);
        }

        // Calculate percentages
        if (totalAllRevenue.compareTo(BigDecimal.ZERO) > 0) {
            for (CategorySalesDto dto : dtos) {
                double pct = dto.getTotalRevenue().multiply(BigDecimal.valueOf(100))
                        .divide(totalAllRevenue, 2, RoundingMode.HALF_UP).doubleValue();
                dto.setPercentage(pct);
            }
        }

        return dtos;
    }

    public List<TopProductDto> getTopSellingProducts(int limit) {
        List<Object[]> rawList = orderItemRepository.findTopSellingProducts(PageRequest.of(0, limit));
        List<TopProductDto> dtos = new ArrayList<>();

        for (Object[] row : rawList) {
            Long productId = ((Number) row[0]).longValue();
            String name = (String) row[1];
            String category = (String) row[2];
            long quantity = ((Number) row[3]).longValue();
            BigDecimal revenue = (BigDecimal) row[4];

            dtos.add(new TopProductDto(productId, name, category, quantity, revenue));
        }

        return dtos;
    }

    public List<PurchaseFrequencyDto> getPurchaseFrequencyDistribution() {
        List<CustomerSummaryDto> customers = customerRepository.findAll().stream()
                .map(c -> {
                    CustomerSummaryDto dto = new CustomerSummaryDto();
                    dto.setId(c.getId());
                    dto.setTotalPurchases(orderRepository.findByCustomerId(c.getId()).size());
                    return dto;
                })
                .collect(Collectors.toList());

        long count1 = 0;
        long count2to3 = 0;
        long count4to6 = 0;
        long count7plus = 0;
        long totalWithPurchases = 0;

        for (CustomerSummaryDto c : customers) {
            long p = c.getTotalPurchases();
            if (p == 1) {
                count1++;
                totalWithPurchases++;
            } else if (p >= 2 && p <= 3) {
                count2to3++;
                totalWithPurchases++;
            } else if (p >= 4 && p <= 6) {
                count4to6++;
                totalWithPurchases++;
            } else if (p >= 7) {
                count7plus++;
                totalWithPurchases++;
            }
        }

        double total = Math.max(1, totalWithPurchases);
        List<PurchaseFrequencyDto> list = new ArrayList<>();
        list.add(new PurchaseFrequencyDto("1 Purchase", count1, Math.round((count1 * 100.0 / total) * 10.0) / 10.0));
        list.add(new PurchaseFrequencyDto("2-3 Purchases", count2to3, Math.round((count2to3 * 100.0 / total) * 10.0) / 10.0));
        list.add(new PurchaseFrequencyDto("4-6 Purchases", count4to6, Math.round((count4to6 * 100.0 / total) * 10.0) / 10.0));
        list.add(new PurchaseFrequencyDto("7+ Purchases", count7plus, Math.round((count7plus * 100.0 / total) * 10.0) / 10.0));

        return list;
    }

    public List<Order> getRecentOrders(int limit) {
        return orderRepository.findRecentOrders(PageRequest.of(0, limit));
    }
}
