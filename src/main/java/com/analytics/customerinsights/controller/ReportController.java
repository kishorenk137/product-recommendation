package com.analytics.customerinsights.controller;

import com.analytics.customerinsights.service.ReportService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.time.LocalDate;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public String viewReports(@RequestParam(value = "type", required = false, defaultValue = "sales") String type,
                              @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                              @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                              Model model) {
        if (startDate == null) {
            startDate = LocalDate.now().minusMonths(6);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        model.addAttribute("reportType", type);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        switch (type.toLowerCase()) {
            case "customers":
                model.addAttribute("reportData", reportService.getCustomerPurchaseReport());
                break;
            case "products":
                model.addAttribute("reportData", reportService.getProductPopularityReport());
                break;
            case "preferences":
                model.addAttribute("reportData", reportService.getCustomerPreferenceReport());
                break;
            case "segments":
                model.addAttribute("reportData", reportService.getCustomerSegmentationReport());
                break;
            case "sales":
            default:
                model.addAttribute("reportData", reportService.getSalesReport(startDate, endDate));
                break;
        }

        model.addAttribute("activeNav", "reports");
        return "reports/index";
    }

    @GetMapping("/export/csv")
    public void exportCsv(@RequestParam(value = "type", required = false, defaultValue = "sales") String type,
                          @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                          @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                          HttpServletResponse response) throws IOException {
        String filename;
        String csvContent;

        switch (type.toLowerCase()) {
            case "customers":
                filename = "customer_purchase_report_" + LocalDate.now() + ".csv";
                csvContent = reportService.exportCustomerPurchaseCsv();
                break;
            case "products":
                filename = "product_popularity_report_" + LocalDate.now() + ".csv";
                csvContent = reportService.exportProductPopularityCsv();
                break;
            case "preferences":
                filename = "customer_preference_report_" + LocalDate.now() + ".csv";
                csvContent = reportService.exportPreferenceCsv();
                break;
            case "segments":
                filename = "customer_segmentation_report_" + LocalDate.now() + ".csv";
                csvContent = reportService.exportSegmentationCsv();
                break;
            case "sales":
            default:
                filename = "sales_report_" + LocalDate.now() + ".csv";
                csvContent = reportService.exportSalesCsv(startDate, endDate);
                break;
        }

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        response.getWriter().write(csvContent);
        response.getWriter().flush();
    }
}
