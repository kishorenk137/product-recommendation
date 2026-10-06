package com.analytics.customerinsights.controller;

import com.analytics.customerinsights.entity.Customer;
import com.analytics.customerinsights.entity.Order;
import com.analytics.customerinsights.service.CustomerService;
import com.analytics.customerinsights.service.ProductService;
import com.analytics.customerinsights.service.PurchaseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/purchases")
public class PurchaseController {

    private final PurchaseService purchaseService;
    private final CustomerService customerService;
    private final ProductService productService;

    public PurchaseController(PurchaseService purchaseService,
                              CustomerService customerService,
                              ProductService productService) {
        this.purchaseService = purchaseService;
        this.customerService = customerService;
        this.productService = productService;
    }

    @GetMapping
    public String listPurchases(@RequestParam(value = "customerId", required = false) Long customerId,
                                @RequestParam(value = "category", required = false, defaultValue = "ALL") String category,
                                @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                @RequestParam(value = "search", required = false) String search,
                                @RequestParam(value = "page", defaultValue = "0") int page,
                                @RequestParam(value = "size", defaultValue = "10") int size,
                                Model model) {
        PageRequest pageRequest = PageRequest.of(Math.max(0, page), size, Sort.by("orderDate").descending());
        Page<Order> orderPage = purchaseService.getOrders(customerId, category, startDate, endDate, search, pageRequest);

        Map<String, Object> metrics = purchaseService.calculatePurchaseMetrics(customerId, startDate, endDate);

        model.addAttribute("orderPage", orderPage);
        model.addAttribute("customers", customerService.getAllCustomers());
        model.addAttribute("categories", productService.getAllCategories());
        model.addAttribute("selectedCustomerId", customerId);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("search", search != null ? search : "");
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("totalElements", orderPage.getTotalElements());

        model.addAttribute("totalSpend", metrics.get("totalSpend"));
        model.addAttribute("totalOrdersCount", metrics.get("orderCount"));
        model.addAttribute("avgOrderValue", metrics.get("avgOrderValue"));
        model.addAttribute("activeNav", "purchases");

        return "purchases/list";
    }

    @GetMapping("/{id}")
    public String viewOrderDetail(@PathVariable Long id, Model model) {
        Order order = purchaseService.getOrderById(id);
        model.addAttribute("order", order);
        model.addAttribute("activeNav", "purchases");
        return "purchases/detail";
    }
}
