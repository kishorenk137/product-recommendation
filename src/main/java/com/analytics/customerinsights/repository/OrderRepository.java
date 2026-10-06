package com.analytics.customerinsights.repository;

import com.analytics.customerinsights.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByCustomerId(Long customerId);

    List<Order> findByCustomerIdOrderByOrderDateDesc(Long customerId);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.product WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") Long id);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.customer c LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.product")
    List<Order> findAllWithDetails();

    @Query("SELECT o FROM Order o JOIN o.customer c WHERE " +
           "(:customerId IS NULL OR c.id = :customerId) AND " +
           "(:status IS NULL OR o.status = :status) AND " +
           "(:startDate IS NULL OR o.orderDate >= :startDate) AND " +
           "(:endDate IS NULL OR o.orderDate <= :endDate) AND " +
           "(:search IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Order> searchOrders(@Param("customerId") Long customerId,
                             @Param("status") String status,
                             @Param("startDate") LocalDateTime startDate,
                             @Param("endDate") LocalDateTime endDate,
                             @Param("search") String search,
                             Pageable pageable);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = 'COMPLETED'")
    BigDecimal calculateTotalRevenue();

    @Query("SELECT AVG(o.totalAmount) FROM Order o WHERE o.status = 'COMPLETED'")
    BigDecimal calculateAverageOrderValue();

    @Query("SELECT o FROM Order o JOIN FETCH o.customer ORDER BY o.orderDate DESC")
    List<Order> findRecentOrders(Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.orderDate >= :since")
    List<Order> findOrdersSince(@Param("since") LocalDateTime since);

    @Query("SELECT o FROM Order o WHERE o.orderDate BETWEEN :start AND :end")
    List<Order> findOrdersBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
