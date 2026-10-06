package com.analytics.customerinsights.service;

import com.analytics.customerinsights.dto.ProductRecommendationDto;
import com.analytics.customerinsights.entity.Customer;
import com.analytics.customerinsights.entity.Order;
import com.analytics.customerinsights.entity.OrderItem;
import com.analytics.customerinsights.entity.Product;
import com.analytics.customerinsights.repository.OrderItemRepository;
import com.analytics.customerinsights.repository.OrderRepository;
import com.analytics.customerinsights.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RecommendationServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private RecommendationService recommendationService;

    private Customer customer;
    private Product product1;
    private Product product2;
    private Product product3;

    @BeforeEach
    void setUp() {
        customer = new Customer("Alex Wright", "alex@example.com", "555-0101", "Seattle", LocalDate.now(), "ACTIVE");
        customer.setId(1L);

        product1 = new Product("Smart TV", "Electronics", new BigDecimal("599.99"), 20, "4K TV", null);
        product1.setId(101L);

        product2 = new Product("Laptop Pro", "Electronics", new BigDecimal("1099.99"), 15, "Pro Laptop", null);
        product2.setId(102L);

        product3 = new Product("Air Fryer", "Home Appliances", new BigDecimal("119.99"), 30, "Air fryer", null);
        product3.setId(103L);
    }

    @Test
    @DisplayName("Should recommend unpurchased products from preferred category")
    void testGetRecommendationsForCustomerWithPurchases() {
        // Customer previously bought product1 (Electronics)
        Order order = new Order(customer, LocalDateTime.now().minusDays(10), new BigDecimal("599.99"), "COMPLETED", "Credit Card");
        OrderItem item1 = new OrderItem(order, product1, 1, product1.getPrice(), product1.getPrice());
        List<OrderItem> customerItems = List.of(item1);

        when(orderRepository.findByCustomerId(1L)).thenReturn(List.of(order));
        when(orderItemRepository.findByCustomerId(1L)).thenReturn(customerItems);
        when(productRepository.findAll()).thenReturn(Arrays.asList(product1, product2, product3));

        // Global order items for popularity calculation
        when(orderItemRepository.findAll()).thenReturn(List.of(item1));

        List<ProductRecommendationDto> recommendations = recommendationService.getRecommendationsForCustomer(1L, 5);

        assertNotNull(recommendations);
        assertFalse(recommendations.isEmpty());

        // Top recommendation should be product2 (Laptop Pro in Electronics, which customer hasn't purchased)
        ProductRecommendationDto topRec = recommendations.get(0);
        assertEquals("Electronics", topRec.getCategory());
        assertTrue(topRec.getConfidenceScore() >= 40.0 && topRec.getConfidenceScore() <= 98.0);
        assertNotNull(topRec.getRecommendationReason());
    }

    @Test
    @DisplayName("Should provide trending recommendations when customer has no past purchases")
    void testGetRecommendationsForNewCustomer() {
        when(orderRepository.findByCustomerId(1L)).thenReturn(new ArrayList<>());
        when(orderItemRepository.findByCustomerId(1L)).thenReturn(new ArrayList<>());
        when(productRepository.findAll()).thenReturn(Arrays.asList(product1, product2, product3));
        when(orderItemRepository.findAll()).thenReturn(new ArrayList<>());

        List<ProductRecommendationDto> recommendations = recommendationService.getRecommendationsForCustomer(1L, 3);

        assertNotNull(recommendations);
        assertEquals(3, recommendations.size());
        assertEquals("Store-Wide Best Seller", recommendations.get(0).getMatchType());
    }

    @Test
    @DisplayName("Should recommend shoes matching dynamic category and buyer preferences")
    void testGetRecommendationsByPreferencesForShoes() {
        Product shoe1 = new Product("Apex Sprint Pro Running Shoes", "Shoes", new BigDecimal("129.99"), 45, "Breathable running shoes with responsive foam cushioning.", null);
        shoe1.setId(201L);
        Product shoe2 = new Product("TrailShield Hiking Shoes", "Shoes", new BigDecimal("149.99"), 30, "Waterproof outdoor hiking shoes.", null);
        shoe2.setId(202L);

        when(productRepository.findAll()).thenReturn(List.of(shoe1, shoe2, product1));

        java.util.Map<String, String> prefs = new java.util.HashMap<>();
        prefs.put("purpose", "Running");
        prefs.put("comfort", "Maximum Cushion");
        prefs.put("priceRange", "$90 - $140");

        List<ProductRecommendationDto> matches = recommendationService.getRecommendationsByPreferences("Shoes", prefs);

        assertNotNull(matches);
        assertFalse(matches.isEmpty());
        ProductRecommendationDto top = matches.get(0);
        assertEquals("Shoes", top.getCategory());
        assertEquals("Apex Sprint Pro Running Shoes", top.getProductName());
        assertTrue(top.getConfidenceScore() >= 70.0);
        assertTrue(top.getRecommendationReason().contains("Running"));
    }
}
