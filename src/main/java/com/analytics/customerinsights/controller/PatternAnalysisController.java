package com.analytics.customerinsights.controller;

import com.analytics.customerinsights.dto.CategorySalesDto;
import com.analytics.customerinsights.dto.CustomerSummaryDto;
import com.analytics.customerinsights.dto.ProductAffinityDto;
import com.analytics.customerinsights.dto.TopProductDto;
import com.analytics.customerinsights.service.DashboardService;
import com.analytics.customerinsights.service.PatternAnalysisService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/patterns")
public class PatternAnalysisController {

    private final PatternAnalysisService patternAnalysisService;
    private final DashboardService dashboardService;

    public PatternAnalysisController(PatternAnalysisService patternAnalysisService,
                                     DashboardService dashboardService) {
        this.patternAnalysisService = patternAnalysisService;
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public String viewPatterns(Model model) {
        List<TopProductDto> topProducts = patternAnalysisService.getFrequentlyPurchasedProducts(8);
        List<CategorySalesDto> popularCategories = patternAnalysisService.getMostPopularCategories();
        List<CustomerSummaryDto> topSpenders = patternAnalysisService.getTopSpendingCustomers(5);
        List<CustomerSummaryDto> frequentBuyers = patternAnalysisService.getMostFrequentCustomers(5);
        Map<String, BigDecimal> seasonalTrends = patternAnalysisService.getSeasonalTrends();
        List<ProductAffinityDto> affinities = patternAnalysisService.getProductsPurchasedTogether(10);
        BigDecimal aov = dashboardService.getDashboardStats().getAverageOrderValue();

        model.addAttribute("topProducts", topProducts);
        model.addAttribute("popularCategories", popularCategories);
        model.addAttribute("topSpenders", topSpenders);
        model.addAttribute("frequentBuyers", frequentBuyers);
        model.addAttribute("seasonalTrends", seasonalTrends);
        model.addAttribute("affinities", affinities);
        model.addAttribute("aov", aov);
        model.addAttribute("activeNav", "patterns");

        return "analytics/patterns";
    }
}
