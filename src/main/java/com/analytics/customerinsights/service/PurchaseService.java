package com.analytics.customerinsights.service;

import com.analytics.customerinsights.entity.Order;
import com.analytics.customerinsights.entity.OrderItem;
import com.analytics.customerinsights.repository.OrderItemRepository;
import com.analytics.customerinsights.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class PurchaseService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public PurchaseService(OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Transactional(readOnly = true)
    public Page<Order> getOrders(Long customerId, String category, LocalDate startDate, LocalDate endDate,
                                 String search, Pageable pageable) {
        LocalDateTime startDateTime = (startDate != null) ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = (endDate != null) ? endDate.atTime(LocalTime.MAX) : null;
        String cleanSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;

        Page<Order> rawOrders = orderRepository.searchOrders(customerId, null, startDateTime, endDateTime, cleanSearch, pageable);

        // If category filter is supplied, filter orders containing items in that category
        if (category != null && !category.trim().isEmpty() && !category.equalsIgnoreCase("ALL")) {
            List<Order> filtered = rawOrders.getContent().stream()
                    .filter(o -> o.getItems().stream().anyMatch(i -> i.getProduct().getCategory().equalsIgnoreCase(category.trim())))
                    .collect(Collectors.toList());
            return new PageImpl<>(filtered, pageable, filtered.size());
        }

        return rawOrders;
    }

    @Transactional(readOnly = true)
    public Order getOrderById(Long id) {
        return orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new NoSuchElementException("Order not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersByCustomer(Long customerId) {
        return orderRepository.findByCustomerIdOrderByOrderDateDesc(customerId);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> calculatePurchaseMetrics(Long customerId, LocalDate startDate, LocalDate endDate) {
        List<Order> orders;
        if (customerId != null) {
            orders = orderRepository.findByCustomerId(customerId);
        } else if (startDate != null || endDate != null) {
            LocalDateTime start = (startDate != null) ? startDate.atStartOfDay() : LocalDateTime.now().minusYears(5);
            LocalDateTime end = (endDate != null) ? endDate.atTime(LocalTime.MAX) : LocalDateTime.now();
            orders = orderRepository.findOrdersBetween(start, end);
        } else {
            orders = orderRepository.findAll();
        }

        BigDecimal totalSpend = orders.stream()
                .filter(o -> "COMPLETED".equalsIgnoreCase(o.getStatus()))
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int orderCount = orders.size();
        BigDecimal avgOrderValue = (orderCount > 0)
                ? totalSpend.divide(BigDecimal.valueOf(orderCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalSpend", totalSpend);
        metrics.put("orderCount", orderCount);
        metrics.put("avgOrderValue", avgOrderValue);

        return metrics;
    }
}
