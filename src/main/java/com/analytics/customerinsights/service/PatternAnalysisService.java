package com.analytics.customerinsights.service;

import com.analytics.customerinsights.dto.CategorySalesDto;
import com.analytics.customerinsights.dto.CustomerSummaryDto;
import com.analytics.customerinsights.dto.ProductAffinityDto;
import com.analytics.customerinsights.dto.TopProductDto;
import com.analytics.customerinsights.entity.Order;
import com.analytics.customerinsights.entity.OrderItem;
import com.analytics.customerinsights.repository.CustomerRepository;
import com.analytics.customerinsights.repository.OrderItemRepository;
import com.analytics.customerinsights.repository.OrderRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PatternAnalysisService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CustomerService customerService;

    public PatternAnalysisService(OrderRepository orderRepository,
                                  OrderItemRepository orderItemRepository,
                                  CustomerService customerService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.customerService = customerService;
    }

    public List<TopProductDto> getFrequentlyPurchasedProducts(int limit) {
        List<Object[]> rawList = orderItemRepository.findTopSellingProducts(PageRequest.of(0, limit));
        List<TopProductDto> dtos = new ArrayList<>();
        for (Object[] row : rawList) {
            dtos.add(new TopProductDto(
                    ((Number) row[0]).longValue(),
                    (String) row[1],
                    (String) row[2],
                    ((Number) row[3]).longValue(),
                    (BigDecimal) row[4]
            ));
        }
        return dtos;
    }

    public List<CategorySalesDto> getMostPopularCategories() {
        List<Object[]> rawList = orderItemRepository.findSalesByCategory();
        BigDecimal totalRevenue = BigDecimal.ZERO;

        List<CategorySalesDto> list = new ArrayList<>();
        for (Object[] row : rawList) {
            String category = (String) row[0];
            BigDecimal revenue = (BigDecimal) row[1];
            long orderCount = ((Number) row[2]).longValue();
            long quantity = ((Number) row[3]).longValue();
            list.add(new CategorySalesDto(category, revenue, orderCount, quantity));
            totalRevenue = totalRevenue.add(revenue);
        }

        if (totalRevenue.compareTo(BigDecimal.ZERO) > 0) {
            for (CategorySalesDto dto : list) {
                double pct = dto.getTotalRevenue().multiply(BigDecimal.valueOf(100))
                        .divide(totalRevenue, 2, RoundingMode.HALF_UP).doubleValue();
                dto.setPercentage(pct);
            }
        }
        return list;
    }

    public List<CustomerSummaryDto> getTopSpendingCustomers(int limit) {
        return customerService.getAllCustomers().stream()
                .map(customerService::mapToSummaryDto)
                .sorted(Comparator.comparing(CustomerSummaryDto::getTotalSpent).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<CustomerSummaryDto> getMostFrequentCustomers(int limit) {
        return customerService.getAllCustomers().stream()
                .map(customerService::mapToSummaryDto)
                .sorted(Comparator.comparing(CustomerSummaryDto::getTotalPurchases).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public Map<String, BigDecimal> getSeasonalTrends() {
        List<Order> orders = orderRepository.findAll();
        Map<String, BigDecimal> seasonalRevenue = new LinkedHashMap<>();
        seasonalRevenue.put("Q1 (Winter)", BigDecimal.ZERO);
        seasonalRevenue.put("Q2 (Spring)", BigDecimal.ZERO);
        seasonalRevenue.put("Q3 (Summer)", BigDecimal.ZERO);
        seasonalRevenue.put("Q4 (Fall/Holiday)", BigDecimal.ZERO);

        for (Order o : orders) {
            if ("COMPLETED".equalsIgnoreCase(o.getStatus())) {
                int month = o.getOrderDate().getMonthValue();
                String quarter;
                if (month <= 3) {
                    quarter = "Q1 (Winter)";
                } else if (month <= 6) {
                    quarter = "Q2 (Spring)";
                } else if (month <= 9) {
                    quarter = "Q3 (Summer)";
                } else {
                    quarter = "Q4 (Fall/Holiday)";
                }
                seasonalRevenue.put(quarter, seasonalRevenue.get(quarter).add(o.getTotalAmount()));
            }
        }
        return seasonalRevenue;
    }

    /**
     * Market Basket Analysis:
     * Calculates pairwise co-occurrence of products in the same orders
     * Returns top product pairs that are commonly bought together.
     */
    public List<ProductAffinityDto> getProductsPurchasedTogether(int limit) {
        List<Order> allOrders = orderRepository.findAllWithDetails();

        // Map pair -> count
        Map<String, Long> pairCounts = new HashMap<>();
        Map<String, String[]> pairDetails = new HashMap<>();
        Map<String, Long> productOrderCount = new HashMap<>();

        for (Order order : allOrders) {
            List<OrderItem> items = order.getItems();
            if (items == null || items.size() < 2) continue;

            // Track individual product presence in order
            Set<String> distinctProductsInOrder = new HashSet<>();
            Map<String, String> productCategoryMap = new HashMap<>();

            for (OrderItem item : items) {
                String pName = item.getProduct().getName();
                distinctProductsInOrder.add(pName);
                productCategoryMap.put(pName, item.getProduct().getCategory());
            }

            for (String p : distinctProductsInOrder) {
                productOrderCount.put(p, productOrderCount.getOrDefault(p, 0L) + 1);
            }

            // Generate unique pairs
            List<String> productList = new ArrayList<>(distinctProductsInOrder);
            for (int i = 0; i < productList.size(); i++) {
                for (int j = i + 1; j < productList.size(); j++) {
                    String a = productList.get(i);
                    String b = productList.get(j);
                    // Standardize key lexicographically
                    String key = (a.compareTo(b) < 0) ? (a + "||" + b) : (b + "||" + a);
                    pairCounts.put(key, pairCounts.getOrDefault(key, 0L) + 1);

                    if (!pairDetails.containsKey(key)) {
                        String first = (a.compareTo(b) < 0) ? a : b;
                        String second = (a.compareTo(b) < 0) ? b : a;
                        pairDetails.put(key, new String[]{
                                first,
                                second,
                                productCategoryMap.get(first),
                                productCategoryMap.get(second)
                        });
                    }
                }
            }
        }

        List<ProductAffinityDto> affinityList = new ArrayList<>();
        long totalMultiItemOrders = allOrders.stream().filter(o -> o.getItems().size() > 1).count();

        for (Map.Entry<String, Long> entry : pairCounts.entrySet()) {
            long count = entry.getValue();
            String[] details = pairDetails.get(entry.getKey());
            String prodA = details[0];
            String prodB = details[1];
            String catA = details[2];
            String catB = details[3];

            long countA = productOrderCount.getOrDefault(prodA, 1L);
            double confidence = (count * 100.0) / countA;
            confidence = Math.round(confidence * 10.0) / 10.0;

            String insight = String.format("Customers buying '%s' also buy '%s' in %.1f%% of orders.",
                    prodA, prodB, confidence);

            affinityList.add(new ProductAffinityDto(prodA, prodB, catA, catB, count, confidence, insight));
        }

        return affinityList.stream()
                .sorted(Comparator.comparing(ProductAffinityDto::getCoOccurrenceCount).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
}
