package com.analytics.customerinsights.controller;

import com.analytics.customerinsights.dto.DashboardStatsDto;
import com.analytics.customerinsights.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        DashboardStatsDto stats = dashboardService.getDashboardStats();
        model.addAttribute("stats", stats);
        model.addAttribute("monthlySales", dashboardService.getMonthlySalesData());
        model.addAttribute("categorySales", dashboardService.getCategorySalesDistribution());
        model.addAttribute("topProducts", dashboardService.getTopSellingProducts(5));
        model.addAttribute("frequencyData", dashboardService.getPurchaseFrequencyDistribution());
        model.addAttribute("recentOrders", dashboardService.getRecentOrders(6));
        model.addAttribute("activeNav", "dashboard");

        return "dashboard";
    }
}
