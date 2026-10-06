package com.analytics.customerinsights.controller;

import com.analytics.customerinsights.dto.ProductRecommendationDto;
import com.analytics.customerinsights.service.ProductService;
import com.analytics.customerinsights.service.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationApiController {

    private final RecommendationService recommendationService;
    private final ProductService productService;

    public RecommendationApiController(RecommendationService recommendationService,
                                       ProductService productService) {
        this.recommendationService = recommendationService;
        this.productService = productService;
    }

    /**
     * Get all active merchandise categories
     */
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(productService.getAllCategories());
    }

    /**
     * Get dynamic questions schema tailored specifically to a product category.
     * Prevents mismatched questions (e.g., never asks battery/storage for Shoes).
     */
    @GetMapping("/questions")
    public ResponseEntity<Map<String, Object>> getQuestionsForCategory(@RequestParam(defaultValue = "Shoes") String category) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("category", category);

        List<Map<String, Object>> questions = new ArrayList<>();

        switch (category.toLowerCase()) {
            case "shoes":
                questions.add(createQuestion("size", "Select Shoe Size", "select",
                        List.of("US 7", "US 8", "US 9", "US 10", "US 11", "US 12")));
                questions.add(createQuestion("purpose", "Primary Activity / Purpose", "select",
                        List.of("Running", "Walking", "Gym & Sports", "Casual Daily", "Outdoor & Hiking")));
                questions.add(createQuestion("comfort", "Comfort & Cushioning Preference", "select",
                        List.of("Maximum Cushion", "Balanced Daily", "Lightweight & Responsive")));
                questions.add(createQuestion("durability", "Durability & Material", "select",
                        List.of("High Durability / Waterproof", "Breathable Mesh", "Classic Full-Grain Leather")));
                questions.add(createQuestion("priceRange", "Budget Range", "select",
                        List.of("Under $90 (Budget)", "$90 - $140 (Mid-Range)", "$140+ (Premium)")));
                questions.add(createQuestion("brandPreference", "Style / Brand Focus", "select",
                        List.of("Athletic Performance", "Urban Streetwear", "Classic Dress")));
                break;

            case "electronics":
                questions.add(createQuestion("displaySize", "Preferred Screen / Display Size", "select",
                        List.of("Compact (11\"-13\")", "Standard Laptop (15\"-16\")", "Large Screen (55\"+ 4K OLED)")));
                questions.add(createQuestion("storage", "Storage & Memory Capacity", "select",
                        List.of("256 GB NVMe SSD", "512 GB NVMe SSD", "1 TB+ High Capacity")));
                questions.add(createQuestion("battery", "Battery Life & Portability Priority", "select",
                        List.of("All-Day Battery (10+ Hours)", "High-Performance Desktop / Plug-in")));
                questions.add(createQuestion("purpose", "Primary Use Case", "select",
                        List.of("Productivity & Office", "Creative Design & Editing", "Entertainment & 4K Streaming")));
                questions.add(createQuestion("priceRange", "Budget Range", "select",
                        List.of("Under $200", "$200 - $600", "$600+ (High-End)")));
                break;

            case "audio & wearables":
                questions.add(createQuestion("formFactor", "Audio / Wearable Type", "select",
                        List.of("Over-Ear ANC Headphones", "In-Ear True Wireless Earbuds", "Health GPS Smartwatch", "Home Theater Soundbar")));
                questions.add(createQuestion("battery", "Battery Longevity Requirement", "select",
                        List.of("24+ Hours ANC Listening", "10-Day Smartwatch Battery", "Standard (6-8 Hours)")));
                questions.add(createQuestion("feature", "Core Priority Feature", "select",
                        List.of("Active Noise Cancellation (ANC)", "SpO2 & Heart Rate GPS Tracking", "Deep Bass / Dolby Atmos", "IPX7 Waterproof Protection")));
                questions.add(createQuestion("priceRange", "Price Budget", "select",
                        List.of("Under $100", "$100 - $200", "$200+ (Premium)")));
                break;

            case "computer accessories":
                questions.add(createQuestion("accessoryType", "Accessory Category", "select",
                        List.of("Mechanical Keyboard", "Ergonomic Mouse", "Curved UltraWide Monitor", "4K Video Webcam")));
                questions.add(createQuestion("connectivity", "Connectivity Mode", "select",
                        List.of("Wireless Bluetooth & 2.4G", "Wired Ultra-Low Latency")));
                questions.add(createQuestion("comfort", "Ergonomic Priority", "select",
                        List.of("Ergonomic Wrist Support", "144Hz High Refresh Gaming", "Tactile Typing Switches", "Studio Audio Streaming")));
                questions.add(createQuestion("priceRange", "Price Range", "select",
                        List.of("Under $80", "$80 - $150", "$150+")));
                break;

            case "smart home":
                questions.add(createQuestion("deviceType", "Smart Home Device", "select",
                        List.of("Robot Vacuum & Mop", "Smart Video Doorbell", "Smart RGB Lighting")));
                questions.add(createQuestion("feature", "Automation Priority", "select",
                        List.of("LiDAR Navigation & Dirt Disposal", "2K HDR Night-Vision Security", "16M Color Schedules & Voice Control")));
                questions.add(createQuestion("priceRange", "Budget Range", "select",
                        List.of("Under $50", "$50 - $150", "$150+")));
                break;

            case "home appliances":
                questions.add(createQuestion("applianceType", "Appliance Focus", "select",
                        List.of("Espresso Coffee Machine", "Digital Air Fryer", "HEPA Air Purifier")));
                questions.add(createQuestion("feature", "Culinary & Air Feature", "select",
                        List.of("15-Bar Italian Pump & Integrated Grinder", "6-Quart Rapid Oil-Free Cooking", "99.97% Smoke & Allergen Filtration")));
                questions.add(createQuestion("priceRange", "Budget Tier", "select",
                        List.of("Under $150", "$150 - $300", "$300+")));
                break;

            default:
                questions.add(createQuestion("purpose", "Primary Purpose", "select",
                        List.of("Daily Everyday", "Performance / Heavy Duty", "Value / Budget")));
                questions.add(createQuestion("priceRange", "Price Range", "select",
                        List.of("Under $100", "$100 - $250", "$250+")));
                break;
        }

        response.put("questions", questions);
        return ResponseEntity.ok(response);
    }

    /**
     * Generate recommendations based on interactive preference selections via POST
     */
    @PostMapping("/quiz")
    public ResponseEntity<List<ProductRecommendationDto>> predictByQuizPost(@RequestBody Map<String, Object> payload) {
        String category = payload.getOrDefault("category", "Shoes").toString();
        @SuppressWarnings("unchecked")
        Map<String, String> preferences = (Map<String, String>) payload.getOrDefault("preferences", Collections.emptyMap());
        List<ProductRecommendationDto> results = recommendationService.getRecommendationsByPreferences(category, preferences);
        return ResponseEntity.ok(results);
    }

    /**
     * Generate recommendations based on interactive preference selections via GET (for easy URL/query testing)
     */
    @GetMapping("/quiz")
    public ResponseEntity<List<ProductRecommendationDto>> predictByQuizGet(
            @RequestParam(defaultValue = "Shoes") String category,
            @RequestParam(required = false) String purpose,
            @RequestParam(required = false) String comfort,
            @RequestParam(required = false) String durability,
            @RequestParam(required = false) String size,
            @RequestParam(required = false) String priceRange,
            @RequestParam(required = false) String displaySize,
            @RequestParam(required = false) String storage,
            @RequestParam(required = false) String battery,
            @RequestParam(required = false) String feature) {

        Map<String, String> prefs = new HashMap<>();
        if (purpose != null) prefs.put("purpose", purpose);
        if (comfort != null) prefs.put("comfort", comfort);
        if (durability != null) prefs.put("durability", durability);
        if (size != null) prefs.put("size", size);
        if (priceRange != null) prefs.put("priceRange", priceRange);
        if (displaySize != null) prefs.put("displaySize", displaySize);
        if (storage != null) prefs.put("storage", storage);
        if (battery != null) prefs.put("battery", battery);
        if (feature != null) prefs.put("feature", feature);

        List<ProductRecommendationDto> results = recommendationService.getRecommendationsByPreferences(category, prefs);
        return ResponseEntity.ok(results);
    }

    /**
     * Get customer purchase history recommendations
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<ProductRecommendationDto>> getCustomerRecommendations(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "8") int limit) {
        List<ProductRecommendationDto> results = recommendationService.getRecommendationsForCustomer(customerId, limit);
        return ResponseEntity.ok(results);
    }

    private Map<String, Object> createQuestion(String key, String label, String type, List<String> options) {
        Map<String, Object> q = new LinkedHashMap<>();
        q.put("key", key);
        q.put("label", label);
        q.put("type", type);
        q.put("options", options);
        return q;
    }
}
