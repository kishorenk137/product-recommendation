package com.analytics.customerinsights.repository;

import com.analytics.customerinsights.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p WHERE (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%'))) AND (:category IS NULL OR p.category = :category)")
    Page<Product> searchProducts(@Param("search") String search, @Param("category") String category, Pageable pageable);

    List<Product> findByCategoryIgnoreCase(String category);

    List<Product> findByCategoryIgnoreCaseAndStockQuantityGreaterThan(String category, Integer minStock);

    @Query("SELECT DISTINCT p.category FROM Product p ORDER BY p.category ASC")
    List<String> findAllCategories();

    @Query("SELECT p.category, COUNT(p) FROM Product p GROUP BY p.category")
    List<Object[]> countProductsByCategory();

    long countByStockQuantityLessThanEqual(int threshold);
}
