package com.analytics.customerinsights.service;

import com.analytics.customerinsights.dto.CustomerPreferencePredictionDto;
import com.analytics.customerinsights.entity.Customer;
import com.analytics.customerinsights.entity.Order;
import com.analytics.customerinsights.entity.OrderItem;
import com.analytics.customerinsights.entity.Product;
import com.analytics.customerinsights.repository.CustomerRepository;
import com.analytics.customerinsights.repository.OrderItemRepository;
import com.analytics.customerinsights.repository.OrderRepository;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PreferencePredictionServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private PreferencePredictionService preferencePredictionService;

    private Customer customer;
    private Product productTech;
    private Product productHome;

    @BeforeEach
    void setUp() {
        customer = new Customer("Sophia Martinez", "sophia@example.com", "555-0102", "Austin", LocalDate.now().minusMonths(6), "ACTIVE");
        customer.setId(2L);

        productTech = new Product("Robot Vacuum", "Smart Home", new BigDecimal("349.99"), 20, "Vacuum", null);
        productTech.setId(201L);

        productHome = new Product("Air Fryer", "Home Appliances", new BigDecimal("119.99"), 40, "Fryer", null);
        productHome.setId(202L);
    }

    @Test
    @DisplayName("Should detect insufficient data when customer has fewer than 2 orders")
    void testInsufficientDataGuardrail() {
        when(customerRepository.findById(2L)).thenReturn(Optional.of(customer));
        when(orderRepository.findByCustomerIdOrderByOrderDateDesc(2L)).thenReturn(List.of(
                new Order(customer, LocalDateTime.now().minusDays(5), new BigDecimal("100"), "COMPLETED", "Credit Card")
        ));
        when(orderItemRepository.findByCustomerId(2L)).thenReturn(List.of(
                new OrderItem(new Order(), productTech, 1, productTech.getPrice(), productTech.getPrice())
        ));

        CustomerPreferencePredictionDto prediction = preferencePredictionService.predictPreferences(2L);

        assertNotNull(prediction);
        assertFalse(prediction.isHasSufficientData());
        assertEquals("Insufficient Data", prediction.getTopPredictedCategory());
        assertEquals(0.0, prediction.getPredictionConfidence());
        assertTrue(prediction.getPredictionReasons().get(0).contains("minimum of 2 orders is required"));
    }

    @Test
    @DisplayName("Should predict preferences with recency weighting when customer has sufficient orders")
    void testPreferencePredictionWithSufficientData() {
        Order order1 = new Order(customer, LocalDateTime.now().minusDays(15), new BigDecimal("349.99"), "COMPLETED", "Credit Card");
        OrderItem item1 = new OrderItem(order1, productTech, 1, productTech.getPrice(), productTech.getPrice());

        Order order2 = new Order(customer, LocalDateTime.now().minusDays(50), new BigDecimal("119.99"), "COMPLETED", "Credit Card");
        OrderItem item2 = new OrderItem(order2, productHome, 1, productHome.getPrice(), productHome.getPrice());

        when(customerRepository.findById(2L)).thenReturn(Optional.of(customer));
        when(orderRepository.findByCustomerIdOrderByOrderDateDesc(2L)).thenReturn(List.of(order1, order2));
        when(orderItemRepository.findByCustomerId(2L)).thenReturn(List.of(item1, item2));

        CustomerPreferencePredictionDto prediction = preferencePredictionService.predictPreferences(2L);

        assertNotNull(prediction);
        assertTrue(prediction.isHasSufficientData());
        assertEquals("Smart Home", prediction.getTopPredictedCategory());
        assertTrue(prediction.getPredictionConfidence() > 0.0);
        assertFalse(prediction.getPredictionReasons().isEmpty());
    }
}
