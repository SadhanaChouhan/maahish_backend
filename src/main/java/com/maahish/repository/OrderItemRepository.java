package com.maahish.repository;

import com.maahish.entity.OrderItem;
import com.maahish.entity.Seller;
import com.maahish.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    long countByProductSeller(Seller seller);

    @Query("""
            SELECT COUNT(DISTINCT oi.order.id) FROM OrderItem oi
            WHERE oi.product.seller.id = :sellerId
            """)
    long countDistinctOrdersBySellerId(@Param("sellerId") Long sellerId);

    @Query("""
            SELECT COUNT(DISTINCT oi.order.id) FROM OrderItem oi
            WHERE oi.product.seller.id = :sellerId AND oi.order.status = :status
            """)
    long countDistinctOrdersBySellerIdAndStatus(@Param("sellerId") Long sellerId,
                                                @Param("status") OrderStatus status);

    @Query("""
            SELECT COALESCE(SUM(oi.price * oi.qty), 0) FROM OrderItem oi
            WHERE oi.product.seller.id = :sellerId
              AND oi.order.paymentStatus = com.maahish.enums.PaymentStatus.COMPLETED
            """)
    BigDecimal sumRevenueBySellerId(@Param("sellerId") Long sellerId);

    @Query("""
            SELECT oi FROM OrderItem oi
            JOIN FETCH oi.order o
            JOIN FETCH oi.product p
            WHERE p.seller.id = :sellerId
              AND (:status IS NULL OR o.status = :status)
            ORDER BY o.createdAt DESC
            """)
    Page<OrderItem> findSellerOrderItems(@Param("sellerId") Long sellerId,
                                         @Param("status") OrderStatus status,
                                         Pageable pageable);

    @Query("""
            SELECT oi FROM OrderItem oi
            JOIN FETCH oi.order o
            JOIN FETCH oi.product p
            WHERE oi.order.id = :orderId AND p.seller.id = :sellerId
            """)
    List<OrderItem> findByOrderIdAndSellerId(@Param("orderId") Long orderId,
                                             @Param("sellerId") Long sellerId);
}
