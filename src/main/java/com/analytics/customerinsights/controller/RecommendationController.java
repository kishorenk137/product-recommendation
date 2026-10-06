package com.analytics.customerinsights.controller;

import com.analytics.customerinsights.dto.CustomerSummaryDto;
import com.analytics.customerinsights.dto.ProductRecommendationDto;
import com.analytics.customerinsights.entity.Customer;
import com.analytics.customerinsights.service.CustomerService;
import com.analytics.customerinsights.service.RecommendationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/recommendations")
public class RecommendationController {

    private final CustomerService customerService;
    private final RecommendationService recommendationService;
    private final com.analytics.customerinsights.service.ProductService productService;

    public RecommendationController(CustomerService customerService,
                                    RecommendationService recommendationService,
                                    com.analytics.customerinsights.service.ProductService productService) {
        this.customerService = customerService;
        this.recommendationService = recommendationService;
        this.productService = productService;
    }

    @GetMapping
    public String viewRecommendations(@RequestParam(value = "customerId", required = false) Long customerId,
                                      @RequestParam(value = "category", required = false, defaultValue = "Shoes") String category,
                                      Model model) {
        List<Customer> allCustomers = customerService.getAllCustomers();
        model.addAttribute("customers", allCustomers);
        model.addAttribute("categories", productService.getAllCategories());
        model.addAttribute("selectedCategory", category);

        // Default to first customer if none specified
        Long targetCustomerId = customerId;
        if (targetCustomerId == null && !allCustomers.isEmpty()) {
            targetCustomerId = allCustomers.get(0).getId();
        }

        if (targetCustomerId != null) {
            Customer selectedCustomer = customerService.getCustomerById(targetCustomerId);
            CustomerSummaryDto customerSummary = customerService.getCustomerSummary(targetCustomerId);
            List<ProductRecommendationDto> recommendations = recommendationService.getRecommendationsForCustomer(targetCustomerId, 8);

            model.addAttribute("selectedCustomer", selectedCustomer);
            model.addAttribute("customerSummary", customerSummary);
            model.addAttribute("recommendations", recommendations);
        }

        model.addAttribute("activeNav", "recommendations");
        return "analytics/recommendations";
    }
}
