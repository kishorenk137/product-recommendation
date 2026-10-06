package com.analytics.customerinsights.controller;

import com.analytics.customerinsights.dto.CustomerFormDto;
import com.analytics.customerinsights.dto.CustomerSummaryDto;
import com.analytics.customerinsights.dto.ProductRecommendationDto;
import com.analytics.customerinsights.entity.Customer;
import com.analytics.customerinsights.entity.Order;
import com.analytics.customerinsights.service.CustomerService;
import com.analytics.customerinsights.service.PurchaseService;
import com.analytics.customerinsights.service.RecommendationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final PurchaseService purchaseService;
    private final RecommendationService recommendationService;

    public CustomerController(CustomerService customerService,
                              PurchaseService purchaseService,
                              RecommendationService recommendationService) {
        this.customerService = customerService;
        this.purchaseService = purchaseService;
        this.recommendationService = recommendationService;
    }

    @GetMapping
    public String listCustomers(@RequestParam(value = "search", required = false) String search,
                                @RequestParam(value = "status", required = false, defaultValue = "ALL") String status,
                                @RequestParam(value = "page", defaultValue = "0") int page,
                                @RequestParam(value = "size", defaultValue = "10") int size,
                                Model model) {
        PageRequest pageRequest = PageRequest.of(Math.max(0, page), size, Sort.by("registrationDate").descending());
        Page<CustomerSummaryDto> customerPage = customerService.getCustomers(search, status, pageRequest);

        model.addAttribute("customerPage", customerPage);
        model.addAttribute("search", search != null ? search : "");
        model.addAttribute("status", status);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", customerPage.getTotalPages());
        model.addAttribute("totalElements", customerPage.getTotalElements());
        model.addAttribute("activeNav", "customers");

        return "customers/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        CustomerFormDto formDto = new CustomerFormDto();
        model.addAttribute("customerForm", formDto);
        model.addAttribute("isEdit", false);
        model.addAttribute("activeNav", "customers");
        return "customers/form";
    }

    @PostMapping("/new")
    public String createCustomer(@Valid @ModelAttribute("customerForm") CustomerFormDto formDto,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", false);
            model.addAttribute("activeNav", "customers");
            return "customers/form";
        }

        try {
            customerService.createCustomer(formDto);
            redirectAttributes.addFlashAttribute("successMessage", "Customer '" + formDto.getName() + "' created successfully!");
            return "redirect:/customers";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("isEdit", false);
            model.addAttribute("activeNav", "customers");
            return "customers/form";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Customer customer = customerService.getCustomerById(id);
        CustomerFormDto formDto = new CustomerFormDto();
        formDto.setId(customer.getId());
        formDto.setName(customer.getName());
        formDto.setEmail(customer.getEmail());
        formDto.setPhone(customer.getPhone());
        formDto.setCity(customer.getCity());
        formDto.setRegistrationDate(customer.getRegistrationDate());
        formDto.setStatus(customer.getStatus());

        model.addAttribute("customerForm", formDto);
        model.addAttribute("isEdit", true);
        model.addAttribute("activeNav", "customers");
        return "customers/form";
    }

    @PostMapping("/edit/{id}")
    public String updateCustomer(@PathVariable Long id,
                                 @Valid @ModelAttribute("customerForm") CustomerFormDto formDto,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", true);
            model.addAttribute("activeNav", "customers");
            return "customers/form";
        }

        try {
            customerService.updateCustomer(id, formDto);
            redirectAttributes.addFlashAttribute("successMessage", "Customer details updated successfully!");
            return "redirect:/customers";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("isEdit", true);
            model.addAttribute("activeNav", "customers");
            return "customers/form";
        }
    }

    @GetMapping("/view/{id}")
    public String viewCustomerProfile(@PathVariable Long id, Model model) {
        Customer customer = customerService.getCustomerWithOrders(id);
        CustomerSummaryDto summary = customerService.getCustomerSummary(id);
        List<Order> orders = purchaseService.getOrdersByCustomer(id);
        List<ProductRecommendationDto> recommendations = recommendationService.getRecommendationsForCustomer(id, 4);

        model.addAttribute("customer", customer);
        model.addAttribute("summary", summary);
        model.addAttribute("orders", orders);
        model.addAttribute("recommendations", recommendations);
        model.addAttribute("activeNav", "customers");

        return "customers/view";
    }

    @PostMapping("/delete/{id}")
    public String deleteCustomer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Customer customer = customerService.getCustomerById(id);
            String name = customer.getName();
            customerService.deleteCustomer(id);
            redirectAttributes.addFlashAttribute("successMessage", "Customer '" + name + "' and associated records deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting customer: " + e.getMessage());
        }
        return "redirect:/customers";
    }
}
