package com.analytics.customerinsights.service;

import com.analytics.customerinsights.dto.CustomerFormDto;
import com.analytics.customerinsights.dto.CustomerSummaryDto;
import com.analytics.customerinsights.entity.Customer;
import com.analytics.customerinsights.entity.Order;
import com.analytics.customerinsights.entity.OrderItem;
import com.analytics.customerinsights.repository.CustomerRepository;
import com.analytics.customerinsights.repository.OrderItemRepository;
import com.analytics.customerinsights.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public CustomerService(CustomerRepository customerRepository,
                           OrderRepository orderRepository,
                           OrderItemRepository orderItemRepository) {
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Transactional(readOnly = true)
    public Page<CustomerSummaryDto> getCustomers(String search, String status, Pageable pageable) {
        String cleanSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        String cleanStatus = (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("ALL")) ? status.trim() : null;

        Page<Customer> customersPage;
        if (cleanSearch != null || cleanStatus != null) {
            customersPage = customerRepository.searchCustomers(cleanSearch != null ? cleanSearch : "", cleanStatus, pageable);
        } else {
            customersPage = customerRepository.findAll(pageable);
        }

        List<CustomerSummaryDto> dtos = customersPage.getContent().stream()
                .map(this::mapToSummaryDto)
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, customersPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Customer not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Customer getCustomerWithOrders(Long id) {
        return customerRepository.findByIdWithOrders(id)
                .orElseThrow(() -> new NoSuchElementException("Customer not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public CustomerSummaryDto getCustomerSummary(Long id) {
        Customer customer = getCustomerById(id);
        return mapToSummaryDto(customer);
    }

    public Customer createCustomer(CustomerFormDto dto) {
        if (customerRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("A customer with this email already exists: " + dto.getEmail());
        }

        Customer customer = new Customer();
        customer.setName(dto.getName());
        customer.setEmail(dto.getEmail());
        customer.setPhone(dto.getPhone());
        customer.setCity(dto.getCity());
        customer.setRegistrationDate(dto.getRegistrationDate() != null ? dto.getRegistrationDate() : LocalDate.now());
        customer.setStatus(dto.getStatus() != null ? dto.getStatus() : "ACTIVE");

        return customerRepository.save(customer);
    }

    public Customer updateCustomer(Long id, CustomerFormDto dto) {
        Customer customer = getCustomerById(id);

        if (!customer.getEmail().equalsIgnoreCase(dto.getEmail()) && customerRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already taken by another customer: " + dto.getEmail());
        }

        customer.setName(dto.getName());
        customer.setEmail(dto.getEmail());
        customer.setPhone(dto.getPhone());
        customer.setCity(dto.getCity());
        if (dto.getRegistrationDate() != null) {
            customer.setRegistrationDate(dto.getRegistrationDate());
        }
        if (dto.getStatus() != null) {
            customer.setStatus(dto.getStatus());
        }

        return customerRepository.save(customer);
    }

    public void deleteCustomer(Long id) {
        Customer customer = getCustomerById(id);
        customerRepository.delete(customer);
    }

    public CustomerSummaryDto mapToSummaryDto(Customer customer) {
        CustomerSummaryDto dto = new CustomerSummaryDto();
        dto.setId(customer.getId());
        dto.setName(customer.getName());
        dto.setEmail(customer.getEmail());
        dto.setPhone(customer.getPhone());
        dto.setCity(customer.getCity());
        dto.setRegistrationDate(customer.getRegistrationDate());
        dto.setStatus(customer.getStatus());

        List<Order> orders = orderRepository.findByCustomerId(customer.getId());
        dto.setTotalPurchases(orders.size());

        BigDecimal totalSpent = orders.stream()
                .filter(o -> "COMPLETED".equalsIgnoreCase(o.getStatus()))
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setTotalSpent(totalSpent);

        // Find preferred category
        List<OrderItem> items = orderItemRepository.findByCustomerId(customer.getId());
        if (!items.isEmpty()) {
            Map<String, Long> categoryCounts = items.stream()
                    .collect(Collectors.groupingBy(i -> i.getProduct().getCategory(), Collectors.counting()));

            String preferred = categoryCounts.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("N/A");
            dto.setPreferredCategory(preferred);
        } else {
            dto.setPreferredCategory("No Purchases Yet");
        }

        // Determine customer segment
        dto.setSegment(calculateCustomerSegment(customer, orders, totalSpent));

        return dto;
    }

    public String calculateCustomerSegment(Customer customer, List<Order> orders, BigDecimal totalSpent) {
        int orderCount = orders.size();
        long daysSinceRegistration = ChronoUnit.DAYS.between(customer.getRegistrationDate(), LocalDate.now());

        if (totalSpent.compareTo(new BigDecimal("1000")) >= 0) {
            return "High-Spending Customer";
        }
        if (orderCount >= 5) {
            return "Frequent Buyer";
        }
        if (orderCount >= 2) {
            return "Occasional Buyer";
        }
        if (daysSinceRegistration <= 60 && orderCount <= 1) {
            return "New Customer";
        }
        return "Inactive Customer";
    }
}
