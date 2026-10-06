package com.analytics.customerinsights.service;

import com.analytics.customerinsights.dto.CategoryPreferenceDto;
import com.analytics.customerinsights.dto.CustomerPreferencePredictionDto;
import com.analytics.customerinsights.entity.Customer;
import com.analytics.customerinsights.entity.Order;
import com.analytics.customerinsights.entity.OrderItem;
import com.analytics.customerinsights.repository.CustomerRepository;
import com.analytics.customerinsights.repository.OrderItemRepository;
import com.analytics.customerinsights.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PreferencePredictionService {

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public PreferencePredictionService(CustomerRepository customerRepository,
                                       OrderRepository orderRepository,
                                       OrderItemRepository orderItemRepository) {
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    /**
     * Transparent, rule-based preference prediction:
     * - Analyzes category frequency and spend
     * - Recency weighting: Recent orders (< 45 days) receive 1.5x multiplier, older orders (>= 45 days) receive 1.0x
     * - Checks minimum purchase threshold (>= 2 orders required for reliable prediction)
     */
    public CustomerPreferencePredictionDto predictPreferences(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new NoSuchElementException("Customer not found with id: " + customerId));

        List<Order> orders = orderRepository.findByCustomerIdOrderByOrderDateDesc(customerId);
        List<OrderItem> items = orderItemRepository.findByCustomerId(customerId);

        CustomerPreferencePredictionDto prediction = new CustomerPreferencePredictionDto();
        prediction.setCustomerId(customer.getId());
        prediction.setCustomerName(customer.getName());
        prediction.setEmail(customer.getEmail());
        prediction.setTotalOrders(orders.size());

        BigDecimal totalSpend = orders.stream()
                .filter(o -> "COMPLETED".equalsIgnoreCase(o.getStatus()))
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        prediction.setTotalSpent(totalSpend);

        // Check sufficient data threshold: at least 2 completed orders required
        if (orders.size() < 2 || items.isEmpty()) {
            prediction.setHasSufficientData(false);
            prediction.setTopPredictedCategory("Insufficient Data");
            prediction.setPredictionConfidence(0.0);
            prediction.getPredictionReasons().add(
                    String.format("Customer has only completed %d order(s). A minimum of 2 orders is required to establish reliable purchase patterns and avoid misleading bias.",
                            orders.size()));
            return prediction;
        }

        prediction.setHasSufficientData(true);
        LocalDateTime now = LocalDateTime.now();

        // Aggregate by category
        Map<String, Long> countMap = new HashMap<>();
        Map<String, BigDecimal> spendMap = new HashMap<>();
        Map<String, Double> weightedScoreMap = new HashMap<>();
        Map<String, Long> daysSinceMap = new HashMap<>();

        long totalQuantity = 0;

        for (OrderItem item : items) {
            String category = item.getProduct().getCategory();
            int qty = item.getQuantity();
            BigDecimal itemTotal = item.getTotalPrice();

            countMap.put(category, countMap.getOrDefault(category, 0L) + qty);
            spendMap.put(category, spendMap.getOrDefault(category, BigDecimal.ZERO).add(itemTotal));
            totalQuantity += qty;

            // Recency calculation
            LocalDateTime orderDate = item.getOrder().getOrderDate();
            long daysAgo = Math.max(0, Duration.between(orderDate, now).toDays());
            daysSinceMap.merge(category, daysAgo, Math::min);

            // Recency multiplier: 1.5x if ordered within last 45 days, 1.2x if within 90 days, 1.0x otherwise
            double recencyMultiplier = (daysAgo <= 45) ? 1.5 : (daysAgo <= 90 ? 1.2 : 1.0);

            // Weighted contribution
            double itemScore = (qty * 10.0 + itemTotal.doubleValue() * 0.1) * recencyMultiplier;
            weightedScoreMap.put(category, weightedScoreMap.getOrDefault(category, 0.0) + itemScore);
        }

        double totalWeightedScore = weightedScoreMap.values().stream().mapToDouble(Double::doubleValue).sum();
        if (totalWeightedScore <= 0) totalWeightedScore = 1.0;

        List<CategoryPreferenceDto> prefList = new ArrayList<>();

        for (String cat : countMap.keySet()) {
            CategoryPreferenceDto dto = new CategoryPreferenceDto();
            dto.setCategory(cat);
            dto.setPurchaseCount(countMap.get(cat));
            dto.setTotalSpent(spendMap.get(cat));
            dto.setDaysSinceLastPurchase(daysSinceMap.get(cat));

            double freqShare = (countMap.get(cat) * 100.0) / Math.max(1, totalQuantity);
            dto.setFrequencyShare(Math.round(freqShare * 10.0) / 10.0);

            double spendShare = 0.0;
            if (totalSpend.compareTo(BigDecimal.ZERO) > 0) {
                spendShare = spendMap.get(cat).multiply(BigDecimal.valueOf(100))
                        .divide(totalSpend, 2, RoundingMode.HALF_UP).doubleValue();
            }
            dto.setSpendShare(spendShare);

            // Final Affinity Score normalized to 0-100%
            double rawScore = (weightedScoreMap.get(cat) * 100.0) / totalWeightedScore;
            double affinityScore = Math.min(100.0, Math.round(rawScore * 10.0) / 10.0);
            dto.setAffinityScore(affinityScore);

            prefList.add(dto);
        }

        // Sort categories by affinity score descending
        prefList.sort(Comparator.comparing(CategoryPreferenceDto::getAffinityScore).reversed());
        prediction.setCategoryPreferences(prefList);

        if (!prefList.isEmpty()) {
            CategoryPreferenceDto top = prefList.get(0);
            prediction.setTopPredictedCategory(top.getCategory());
            prediction.setPredictionConfidence(top.getAffinityScore());

            // Generate explainable reasons
            prediction.getPredictionReasons().add(String.format(
                    "Highest historical affinity in '%s' accounting for %.1f%% of overall purchases and $%.2f in total spending.",
                    top.getCategory(), top.getFrequencyShare(), top.getTotalSpent()
            ));

            if (top.getDaysSinceLastPurchase() != null && top.getDaysSinceLastPurchase() <= 45) {
                prediction.getPredictionReasons().add(String.format(
                    "High recency boost: customer placed an order in '%s' just %d days ago.",
                    top.getCategory(), top.getDaysSinceLastPurchase()
                ));
            }

            if (prefList.size() > 1) {
                CategoryPreferenceDto second = prefList.get(1);
                prediction.getPredictionReasons().add(String.format(
                    "Secondary interest in '%s' (%.1f%% affinity score, %d items purchased).",
                    second.getCategory(), second.getAffinityScore(), second.getPurchaseCount()
                ));
            }
        }

        return prediction;
    }
}
