package com.analytics.customerinsights.repository;

import com.analytics.customerinsights.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByEmail(String email);
    boolean existsByEmail(String email);

    Page<Customer> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email, Pageable pageable);

    Page<Customer> findByStatus(String status, Pageable pageable);

    @Query("SELECT c FROM Customer c WHERE (LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%'))) AND (:status IS NULL OR c.status = :status)")
    Page<Customer> searchCustomers(@Param("search") String search, @Param("status") String status, Pageable pageable);

    long countByRegistrationDateAfter(LocalDate date);

    @Query("SELECT c FROM Customer c LEFT JOIN FETCH c.orders WHERE c.id = :id")
    Optional<Customer> findByIdWithOrders(@Param("id") Long id);

    @Query("SELECT c FROM Customer c LEFT JOIN FETCH c.orders")
    List<Customer> findAllWithOrders();
}
