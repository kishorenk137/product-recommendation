package com.analytics.customerinsights.repository;

import com.analytics.customerinsights.entity.OrderItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);

    @Query("SELECT oi FROM OrderItem oi JOIN FETCH oi.order o JOIN FETCH oi.product p WHERE o.customer.id = :customerId ORDER BY o.orderDate DESC")
    List<OrderItem> findByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT oi.product.id, oi.product.name, oi.product.category, SUM(oi.quantity), SUM(oi.totalPrice) " +
           "FROM OrderItem oi WHERE oi.order.status = 'COMPLETED' " +
           "GROUP BY oi.product.id, oi.product.name, oi.product.category " +
           "ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> findTopSellingProducts(Pageable pageable);

    @Query("SELECT oi.product.category, SUM(oi.totalPrice), COUNT(DISTINCT oi.order.id), SUM(oi.quantity) " +
           "FROM OrderItem oi WHERE oi.order.status = 'COMPLETED' " +
           "GROUP BY oi.product.category " +
           "ORDER BY SUM(oi.totalPrice) DESC")
    List<Object[]> findSalesByCategory();

    @Query("SELECT oi FROM OrderItem oi JOIN FETCH oi.order o JOIN FETCH oi.product p WHERE o.orderDate BETWEEN :start AND :end")
    List<OrderItem> findItemsBetweenDates(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT oi FROM OrderItem oi JOIN FETCH oi.order o JOIN FETCH oi.product p")
    List<OrderItem> findAllWithProductAndOrder();
}
