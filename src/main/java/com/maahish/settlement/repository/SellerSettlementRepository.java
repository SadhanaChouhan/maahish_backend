package com.maahish.settlement.repository;

import com.maahish.settlement.entity.SellerSettlement;
import com.maahish.settlement.enums.SettlementStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface SellerSettlementRepository extends JpaRepository<SellerSettlement, Long> {

    boolean existsByOrderItemId(Long orderItemId);

    @EntityGraph(attributePaths = {"seller", "order", "orderItem", "orderItem.product"})
    Page<SellerSettlement> findBySettlementStatusOrderByCreatedAtDesc(SettlementStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"seller", "order", "orderItem", "orderItem.product"})
    Page<SellerSettlement> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"seller", "order", "orderItem", "orderItem.product"})
    Page<SellerSettlement> findBySellerIdOrderByCreatedAtDesc(Long sellerId, Pageable pageable);

    @EntityGraph(attributePaths = {"seller", "order", "orderItem", "orderItem.product"})
    Page<SellerSettlement> findBySellerIdAndSettlementStatusOrderByCreatedAtDesc(
            Long sellerId, SettlementStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"seller", "order", "orderItem", "orderItem.product"})
    Optional<SellerSettlement> findWithDetailsById(Long id);

    long countBySettlementStatus(SettlementStatus status);

    long countBySellerIdAndSettlementStatus(Long sellerId, SettlementStatus status);

    @EntityGraph(attributePaths = {"seller", "order", "orderItem", "orderItem.product"})
    List<SellerSettlement> findByOrderIdOrderByCreatedAtAsc(Long orderId);

    @EntityGraph(attributePaths = {"seller", "order", "orderItem", "orderItem.product"})
    List<SellerSettlement> findByOrderIdAndSellerIdOrderByCreatedAtAsc(Long orderId, Long sellerId);

    @Query("SELECT COALESCE(SUM(s.commissionAmount), 0) FROM SellerSettlement s")
    BigDecimal sumTotalCommissionEarned();

    @Query("""
            SELECT COALESCE(SUM(s.netSellerAmount), 0) FROM SellerSettlement s
            WHERE s.seller.id = :sellerId AND s.settlementStatus = :status
            """)
    BigDecimal sumNetAmountBySellerIdAndStatus(@Param("sellerId") Long sellerId,
                                               @Param("status") SettlementStatus status);

    @Query("""
            SELECT COALESCE(SUM(s.commissionAmount), 0) FROM SellerSettlement s
            WHERE s.seller.id = :sellerId
            """)
    BigDecimal sumCommissionBySellerId(@Param("sellerId") Long sellerId);

    @Query("""
            SELECT COALESCE(SUM(s.netSellerAmount), 0) FROM SellerSettlement s
            WHERE s.seller.id = :sellerId
            """)
    BigDecimal sumNetEarningsBySellerId(@Param("sellerId") Long sellerId);

    @Query("""
            SELECT COALESCE(SUM(s.grossAmount), 0) FROM SellerSettlement s
            WHERE s.seller.id = :sellerId
            """)
    BigDecimal sumGrossSalesBySellerId(@Param("sellerId") Long sellerId);

    @Query("""
            SELECT COALESCE(SUM(s.netSellerAmount), 0) FROM SellerSettlement s
            WHERE s.settlementStatus IN :statuses
            """)
    BigDecimal sumNetAmountByStatuses(@Param("statuses") java.util.Collection<SettlementStatus> statuses);
}
