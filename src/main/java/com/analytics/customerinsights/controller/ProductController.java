package com.analytics.customerinsights.controller;

import com.analytics.customerinsights.dto.ProductFormDto;
import com.analytics.customerinsights.entity.Product;
import com.analytics.customerinsights.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public String listProducts(@RequestParam(value = "search", required = false) String search,
                               @RequestParam(value = "category", required = false, defaultValue = "ALL") String category,
                               @RequestParam(value = "page", defaultValue = "0") int page,
                               @RequestParam(value = "size", defaultValue = "10") int size,
                               Model model) {
        PageRequest pageRequest = PageRequest.of(Math.max(0, page), size, Sort.by("name").ascending());
        Page<Product> productPage = productService.getProducts(search, category, pageRequest);

        model.addAttribute("productPage", productPage);
        model.addAttribute("categories", productService.getAllCategories());
        model.addAttribute("search", search != null ? search : "");
        model.addAttribute("selectedCategory", category);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalElements", productPage.getTotalElements());
        model.addAttribute("lowStockCount", productService.getLowStockCount(20));
        model.addAttribute("activeNav", "products");

        return "products/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        ProductFormDto formDto = new ProductFormDto();
        model.addAttribute("productForm", formDto);
        model.addAttribute("categories", productService.getAllCategories());
        model.addAttribute("isEdit", false);
        model.addAttribute("activeNav", "products");
        return "products/form";
    }

    @PostMapping("/new")
    public String createProduct(@Valid @ModelAttribute("productForm") ProductFormDto formDto,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", productService.getAllCategories());
            model.addAttribute("isEdit", false);
            model.addAttribute("activeNav", "products");
            return "products/form";
        }

        productService.createProduct(formDto);
        redirectAttributes.addFlashAttribute("successMessage", "Product '" + formDto.getName() + "' created successfully!");
        return "redirect:/products";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id);
        ProductFormDto formDto = new ProductFormDto();
        formDto.setId(product.getId());
        formDto.setName(product.getName());
        formDto.setCategory(product.getCategory());
        formDto.setPrice(product.getPrice());
        formDto.setStockQuantity(product.getStockQuantity());
        formDto.setDescription(product.getDescription());
        formDto.setImageUrl(product.getImageUrl());

        model.addAttribute("productForm", formDto);
        model.addAttribute("categories", productService.getAllCategories());
        model.addAttribute("isEdit", true);
        model.addAttribute("activeNav", "products");
        return "products/form";
    }

    @PostMapping("/edit/{id}")
    public String updateProduct(@PathVariable Long id,
                                @Valid @ModelAttribute("productForm") ProductFormDto formDto,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", productService.getAllCategories());
            model.addAttribute("isEdit", true);
            model.addAttribute("activeNav", "products");
            return "products/form";
        }

        productService.updateProduct(id, formDto);
        redirectAttributes.addFlashAttribute("successMessage", "Product '" + formDto.getName() + "' updated successfully!");
        return "redirect:/products";
    }

    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Product product = productService.getProductById(id);
            String name = product.getName();
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("successMessage", "Product '" + name + "' deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cannot delete product because it has associated customer orders. Set its stock to 0 instead.");
        }
        return "redirect:/products";
    }
}
