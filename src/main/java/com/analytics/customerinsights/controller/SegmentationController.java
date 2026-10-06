package com.analytics.customerinsights.controller;

import com.analytics.customerinsights.dto.CustomerSegmentDto;
import com.analytics.customerinsights.service.SegmentationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/segments")
public class SegmentationController {

    private final SegmentationService segmentationService;

    public SegmentationController(SegmentationService segmentationService) {
        this.segmentationService = segmentationService;
    }

    @GetMapping
    public String viewSegments(Model model) {
        List<CustomerSegmentDto> segments = segmentationService.getCustomerSegments();

        model.addAttribute("segments", segments);
        model.addAttribute("highSpendingThreshold", segmentationService.getHighSpendingThreshold());
        model.addAttribute("frequentOrdersThreshold", segmentationService.getFrequentOrdersThreshold());
        model.addAttribute("newCustomerDays", segmentationService.getNewCustomerDays());
        model.addAttribute("activeNav", "segments");

        return "analytics/segments";
    }
}
