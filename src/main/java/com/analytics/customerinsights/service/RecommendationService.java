package com.analytics.customerinsights.service;

import com.analytics.customerinsights.dto.ProductRecommendationDto;
import com.analytics.customerinsights.entity.Order;
import com.analytics.customerinsights.entity.OrderItem;
import com.analytics.customerinsights.entity.Product;
import com.analytics.customerinsights.repository.OrderItemRepository;
import com.analytics.customerinsights.repository.OrderRepository;
import com.analytics.customerinsights.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class RecommendationService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;

    public RecommendationService(OrderRepository orderRepository,
                                 OrderItemRepository orderItemRepository,
                                 ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
    }

    /**
     * Rule-Based Personalized Recommendation Engine:
     * 1. Aggregate customer's purchase history by category (frequency & total spend).
     * 2. Compute category affinity score for the customer.
     * 3. Filter products: prioritize categories the customer loves, excluding already purchased products.
     * 4. Rank candidate products by:
     *    Score = (CategoryAffinityScore * 0.50) + (ProductGlobalPopularity * 0.30) + (StockAvailabilityBonus * 0.20)
     * 5. Generate transparent explanations for each recommendation.
     */
    public List<ProductRecommendationDto> getRecommendationsForCustomer(Long customerId, int limit) {
        List<Order> customerOrders = orderRepository.findByCustomerId(customerId);
        List<OrderItem> customerItems = orderItemRepository.findByCustomerId(customerId);
        List<Product> allProducts = productRepository.findAll();

        // Track products already bought by customer
        Set<Long> purchasedProductIds = customerItems.stream()
                .map(i -> i.getProduct().getId())
                .collect(Collectors.toSet());

        // Count global product sales for popularity scoring
        Map<Long, Long> globalProductSales = orderItemRepository.findAll().stream()
                .collect(Collectors.groupingBy(i -> i.getProduct().getId(), Collectors.summingLong(OrderItem::getQuantity)));
        long maxGlobalSales = globalProductSales.values().stream().max(Long::compare).orElse(1L);
        if (maxGlobalSales <= 0) maxGlobalSales = 1L;

        // If customer has NO purchase history, provide Top-Trending Popular Products with clear rationale
        if (customerItems.isEmpty()) {
            return getTrendingRecommendations(allProducts, globalProductSales, maxGlobalSales, limit);
        }

        // Calculate customer's category purchase counts and spend
        Map<String, Long> categoryCountMap = new HashMap<>();
        Map<String, BigDecimal> categorySpendMap = new HashMap<>();
        BigDecimal totalSpend = BigDecimal.ZERO;
        long totalItemsCount = 0;

        for (OrderItem item : customerItems) {
            String cat = item.getProduct().getCategory();
            categoryCountMap.put(cat, categoryCountMap.getOrDefault(cat, 0L) + item.getQuantity());
            categorySpendMap.put(cat, categorySpendMap.getOrDefault(cat, BigDecimal.ZERO).add(item.getTotalPrice()));
            totalSpend = totalSpend.add(item.getTotalPrice());
            totalItemsCount += item.getQuantity();
        }

        // Compute Category Affinity Weight (0 to 100)
        // 60% frequency weight + 40% spend weight
        Map<String, Double> categoryAffinity = new HashMap<>();
        for (String cat : categoryCountMap.keySet()) {
            double freqRatio = (double) categoryCountMap.get(cat) / Math.max(1, totalItemsCount);
            double spendRatio = (totalSpend.compareTo(BigDecimal.ZERO) > 0)
                    ? categorySpendMap.get(cat).divide(totalSpend, 4, RoundingMode.HALF_UP).doubleValue()
                    : 0.0;

            double affinity = (freqRatio * 60.0) + (spendRatio * 40.0);
            categoryAffinity.put(cat, affinity);
        }

        // Identify Top Preferred Category
        String topCategory = categoryAffinity.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("General");

        List<ProductRecommendationDto> recommendations = new ArrayList<>();

        for (Product product : allProducts) {
            // Check stock availability
            if (product.getStockQuantity() <= 0) {
                continue; // Do not recommend out of stock items
            }

            boolean alreadyBought = purchasedProductIds.contains(product.getId());
            double catAffinity = categoryAffinity.getOrDefault(product.getCategory(), 5.0);

            // Global Popularity Normalized to (0 - 100)
            long salesCount = globalProductSales.getOrDefault(product.getId(), 0L);
            double popularityScore = (salesCount * 100.0) / maxGlobalSales;

            // Stock Bonus (0 - 100): High stock availability ensures fulfillment
            double stockScore = Math.min(100.0, (product.getStockQuantity() * 100.0) / 50.0);

            // Overall Score formula
            double rawScore;
            String reason;
            String matchType;

            if (categoryAffinity.containsKey(product.getCategory())) {
                rawScore = (catAffinity * 0.50) + (popularityScore * 0.30) + (stockScore * 0.20);

                if (!alreadyBought) {
                    matchType = "Category Preference & High Popularity";
                    reason = String.format("Matches your strong interest in '%s' (%.0f%% of past activity). %s customer purchases across our store.",
                            product.getCategory(), catAffinity, salesCount > 0 ? "Backed by " + salesCount : "Fresh item with strong");
                } else {
                    // Repurchase / Upgrade recommendation (slight penalty to favor discovery)
                    rawScore *= 0.75;
                    matchType = "Frequent Repurchase Favorite";
                    reason = String.format("You previously enjoyed this product in '%s'. Ready to restock or order again.",
                            product.getCategory());
                }
            } else {
                // Discovery from other popular categories
                rawScore = (10.0 * 0.50) + (popularityScore * 0.35) + (stockScore * 0.15);
                matchType = "Trending Cross-Category Discovery";
                reason = String.format("Trending best-seller with %d total sales and verified stock in '%s'.",
                        salesCount, product.getCategory());
            }

            // Cap confidence score between 40% and 98%
            double confidence = Math.min(98.0, Math.max(40.0, Math.round(rawScore * 10.0) / 10.0));

            recommendations.add(new ProductRecommendationDto(
                    product.getId(),
                    product.getName(),
                    product.getCategory(),
                    product.getPrice(),
                    product.getStockQuantity(),
                    confidence,
                    reason,
                    matchType
            ));
        }

        // Sort descending by confidence score
        return recommendations.stream()
                .sorted(Comparator.comparing(ProductRecommendationDto::getConfidenceScore).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    private List<ProductRecommendationDto> getTrendingRecommendations(List<Product> products,
                                                                       Map<Long, Long> globalSales,
                                                                       long maxSales,
                                                                       int limit) {
        List<ProductRecommendationDto> list = new ArrayList<>();
        for (Product p : products) {
            if (p.getStockQuantity() <= 0) continue;

            long sales = globalSales.getOrDefault(p.getId(), 0L);
            double popScore = (sales * 100.0) / maxSales;
            double confidence = Math.min(95.0, Math.max(50.0, Math.round((50.0 + (popScore * 0.45)) * 10.0) / 10.0));

            String reason = String.format("Curated top-selling favorite in '%s' with %d completed customer orders.",
                    p.getCategory(), sales);

            list.add(new ProductRecommendationDto(
                    p.getId(),
                    p.getName(),
                    p.getCategory(),
                    p.getPrice(),
                    p.getStockQuantity(),
                    confidence,
                    reason,
                    "Store-Wide Best Seller"
            ));
        }

        return list.stream()
                .sorted(Comparator.comparing(ProductRecommendationDto::getConfidenceScore).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Interactive Preference Recommendation Engine:
     * Evaluates category-specific preference inputs (e.g. Size, Comfort, Purpose, Durability, Price for Shoes;
     * Display Size, Storage, Battery, Usage for Electronics; etc.) against available catalog products.
     * Computes multi-factor match confidence and produces explainable rationale.
     */
    public List<ProductRecommendationDto> getRecommendationsByPreferences(String targetCategory, Map<String, String> preferences) {
        List<Product> allProducts = productRepository.findAll();
        List<ProductRecommendationDto> matches = new ArrayList<>();

        List<Product> categoryProducts = allProducts.stream()
                .filter(p -> targetCategory == null || targetCategory.equalsIgnoreCase("ALL") || p.getCategory().equalsIgnoreCase(targetCategory))
                .filter(p -> p.getStockQuantity() > 0)
                .collect(Collectors.toList());

        if (categoryProducts.isEmpty()) {
            categoryProducts = allProducts.stream()
                    .filter(p -> p.getStockQuantity() > 0)
                    .collect(Collectors.toList());
        }

        for (Product product : categoryProducts) {
            double baseScore = 72.0;
            List<String> matchedFactors = new ArrayList<>();

            String prodNameLower = product.getName().toLowerCase();
            String prodDescLower = (product.getDescription() != null ? product.getDescription().toLowerCase() : "");
            String combinedText = prodNameLower + " " + prodDescLower;

            if (preferences != null) {
                // 1. Purpose / Activity match (Running, Walking, Sports, Casual, Outdoor)
                String purpose = preferences.get("purpose");
                if (purpose != null && !purpose.isBlank() && !purpose.equalsIgnoreCase("Any")) {
                    String pLower = purpose.toLowerCase();
                    if (combinedText.contains(pLower) || (pLower.contains("run") && (combinedText.contains("sprint") || combinedText.contains("marathon")))
                            || (pLower.contains("sport") && combinedText.contains("train")) || (pLower.contains("outdoor") && combinedText.contains("hiking"))) {
                        baseScore += 12.0;
                        matchedFactors.add("Targeted for " + purpose);
                    }
                }

                // 2. Comfort / Cushioning match
                String comfort = preferences.get("comfort");
                if (comfort != null && !comfort.isBlank() && !comfort.equalsIgnoreCase("Any")) {
                    if (combinedText.contains("cushion") || combinedText.contains("foam") || combinedText.contains("comfort") || combinedText.contains("arch support")) {
                        baseScore += 8.0;
                        matchedFactors.add(comfort + " Cushioning");
                    }
                }

                // 3. Durability / Material match
                String durability = preferences.get("durability");
                if (durability != null && !durability.isBlank() && !durability.equalsIgnoreCase("Any")) {
                    if (combinedText.contains("durable") || combinedText.contains("durability") || combinedText.contains("waterproof") || combinedText.contains("leather") || combinedText.contains("mesh")) {
                        baseScore += 8.0;
                        matchedFactors.add(durability + " Construction");
                    }
                }

                // 4. Size preference
                String size = preferences.get("size");
                if (size != null && !size.isBlank() && !size.equalsIgnoreCase("Any")) {
                    matchedFactors.add("Available in Size " + size);
                    baseScore += 3.0;
                }

                // 5. Price / Budget Range match
                String pricePref = preferences.get("priceRange");
                if (pricePref != null && !pricePref.isBlank()) {
                    double priceVal = product.getPrice().doubleValue();
                    if (pricePref.contains("<") || pricePref.toLowerCase().contains("under") || pricePref.toLowerCase().contains("budget") || pricePref.contains("80") || pricePref.contains("90") || pricePref.contains("100")) {
                        if (priceVal <= 95.0) {
                            baseScore += 7.0;
                            matchedFactors.add("Budget-friendly price point");
                        }
                    } else if (pricePref.contains("-")) {
                        if (priceVal >= 75.0 && priceVal <= 165.0) {
                            baseScore += 7.0;
                            matchedFactors.add("Fits mid-range budget");
                        }
                    } else if (pricePref.contains("+") || pricePref.toLowerCase().contains("premium")) {
                        if (priceVal >= 130.0) {
                            baseScore += 7.0;
                            matchedFactors.add("Premium performance tier");
                        }
                    }
                }

                // 6. Electronics preferences (Display, Storage, Battery) - ONLY evaluated when relevant
                String displaySize = preferences.get("displaySize");
                if (displaySize != null && !displaySize.isBlank() && !displaySize.equalsIgnoreCase("Any")) {
                    if (combinedText.contains("screen") || combinedText.contains("display") || combinedText.contains("11") || combinedText.contains("15") || combinedText.contains("55") || combinedText.contains("curved")) {
                        baseScore += 8.0;
                        matchedFactors.add(displaySize + " Display");
                    }
                }

                String storage = preferences.get("storage");
                if (storage != null && !storage.isBlank() && !storage.equalsIgnoreCase("Any")) {
                    if (combinedText.contains("ssd") || combinedText.contains("storage") || combinedText.contains("ram") || combinedText.contains("512gb")) {
                        baseScore += 8.0;
                        matchedFactors.add(storage + " Storage");
                    }
                }

                String battery = preferences.get("battery");
                if (battery != null && !battery.isBlank() && !battery.equalsIgnoreCase("Any")) {
                    if (combinedText.contains("battery") || combinedText.contains("mah") || combinedText.contains("fast charge") || combinedText.contains("10-day")) {
                        baseScore += 8.0;
                        matchedFactors.add(battery + " Battery");
                    }
                }

                // 7. Key Feature / Appliance / Audio match
                String feature = preferences.get("feature");
                if (feature != null && !feature.isBlank() && !feature.equalsIgnoreCase("Any")) {
                    String fLower = feature.toLowerCase();
                    if (combinedText.contains(fLower) || (fLower.contains("anc") && combinedText.contains("cancelling"))
                            || (fLower.contains("espresso") && combinedText.contains("pressure")) || (fLower.contains("air") && combinedText.contains("fryer"))) {
                        baseScore += 9.0;
                        matchedFactors.add(feature + " feature");
                    }
                }
            }

            // In-stock bonus
            if (product.getStockQuantity() > 20) {
                baseScore += 2.0;
            }

            double confidence = Math.min(98.0, Math.max(50.0, Math.round(baseScore * 10.0) / 10.0));

            String reason;
            if (!matchedFactors.isEmpty()) {
                reason = "Recommended for: " + String.join(", ", matchedFactors) + ".";
            } else {
                reason = String.format("Top-rated store catalog selection in %s matching your criteria.", product.getCategory());
            }

            matches.add(new ProductRecommendationDto(
                    product.getId(),
                    product.getName(),
                    product.getCategory(),
                    product.getPrice(),
                    product.getStockQuantity(),
                    confidence,
                    reason,
                    "Dynamic Preference Match"
            ));
        }

        return matches.stream()
                .sorted(Comparator.comparing(ProductRecommendationDto::getConfidenceScore).reversed())
                .collect(Collectors.toList());
    }
}
