package com.maahish.order.repository;

import com.maahish.order.entity.Order;
import com.maahish.order.enums.OrderStatus;
import com.maahish.payment.enums.PaymentStatus;
import com.maahish.user.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(String orderNumber);

    Optional<Order> findFirstByOrderNumberStartingWithOrderByOrderNumberDesc(String prefix);

    @EntityGraph(attributePaths = {"user", "address", "items", "items.product", "items.product.seller", "payment"})
    Page<Order> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "address", "items", "items.product", "items.product.seller", "payment"})
    Page<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "address", "items", "items.product", "items.product.seller", "payment"})
    Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"user", "address", "items", "items.product", "items.product.seller", "payment"})
    Optional<Order> findWithDetailsById(Long id);

    @EntityGraph(attributePaths = {"user", "address", "items", "items.product", "items.product.seller", "payment"})
    Optional<Order> findWithDetailsByOrderNumber(String orderNumber);

    @EntityGraph(attributePaths = {"payment"})
    Optional<Order> findFirstByUserAndPaymentStatusOrderByCreatedAtDesc(User user, PaymentStatus paymentStatus);

    long countByStatus(OrderStatus status);

    @EntityGraph(attributePaths = {"user", "address", "items", "items.product", "items.product.seller", "payment"})
    @org.springframework.data.jpa.repository.Query("""
            SELECT DISTINCT o FROM Order o
            JOIN o.items oi
            JOIN oi.product p
            WHERE p.seller.id = :sellerId
              AND (:status IS NULL OR o.status = :status)
            ORDER BY o.createdAt DESC
            """)
    Page<Order> findOrdersContainingSellerProducts(@org.springframework.data.repository.query.Param("sellerId") Long sellerId,
                                                   @org.springframework.data.repository.query.Param("status") OrderStatus status,
                                                   Pageable pageable);
}
