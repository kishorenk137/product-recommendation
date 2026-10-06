package com.analytics.customerinsights.controller;

import com.analytics.customerinsights.dto.CustomerPreferencePredictionDto;
import com.analytics.customerinsights.dto.CustomerSummaryDto;
import com.analytics.customerinsights.entity.Customer;
import com.analytics.customerinsights.service.CustomerService;
import com.analytics.customerinsights.service.PreferencePredictionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/predictions")
public class PreferencePredictionController {

    private final CustomerService customerService;
    private final PreferencePredictionService preferencePredictionService;

    public PreferencePredictionController(CustomerService customerService,
                                         PreferencePredictionService preferencePredictionService) {
        this.customerService = customerService;
        this.preferencePredictionService = preferencePredictionService;
    }

    @GetMapping
    public String viewPredictions(@RequestParam(value = "customerId", required = false) Long customerId,
                                  Model model) {
        List<Customer> allCustomers = customerService.getAllCustomers();
        model.addAttribute("customers", allCustomers);

        Long targetCustomerId = customerId;
        if (targetCustomerId == null && !allCustomers.isEmpty()) {
            targetCustomerId = allCustomers.get(0).getId();
        }

        if (targetCustomerId != null) {
            Customer selectedCustomer = customerService.getCustomerById(targetCustomerId);
            CustomerSummaryDto customerSummary = customerService.getCustomerSummary(targetCustomerId);
            CustomerPreferencePredictionDto prediction = preferencePredictionService.predictPreferences(targetCustomerId);

            model.addAttribute("selectedCustomer", selectedCustomer);
            model.addAttribute("customerSummary", customerSummary);
            model.addAttribute("prediction", prediction);
        }

        model.addAttribute("activeNav", "predictions");
        return "analytics/predictions";
    }
}
