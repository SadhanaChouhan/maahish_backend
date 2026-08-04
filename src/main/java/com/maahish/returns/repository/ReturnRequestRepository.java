package com.maahish.returns.repository;

import com.maahish.returns.entity.ReturnRequest;
import com.maahish.returns.enums.ReturnRequestStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, Long> {

    Optional<ReturnRequest> findByReturnNumber(String returnNumber);

    Optional<ReturnRequest> findByOrderItemId(Long orderItemId);

    Page<ReturnRequest> findByCustomerIdOrderByCreatedAtDesc(Long customerId, Pageable pageable);

    long countByCustomerId(Long customerId);

    boolean existsByCustomerIdAndStatusIn(Long customerId, Collection<ReturnRequestStatus> statuses);

    Page<ReturnRequest> findBySellerIdOrderByCreatedAtDesc(Long sellerId, Pageable pageable);

    Page<ReturnRequest> findByStatusOrderByCreatedAtDesc(ReturnRequestStatus status, Pageable pageable);

    Page<ReturnRequest> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT r FROM ReturnRequest r WHERE r.status = :status AND r.shipReminderSent = false AND r.updatedAt < :before")
    List<ReturnRequest> findApprovedAwaitingShipment(@Param("status") ReturnRequestStatus status,
                                                     @Param("before") LocalDateTime before);

    @EntityGraph(attributePaths = {
            "order", "orderItem", "orderItem.product", "customer", "seller", "images", "shipment", "history"
    })
    Optional<ReturnRequest> findWithDetailsById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {
            "order", "order.payment", "orderItem", "orderItem.product", "customer", "seller"
    })
    @Query("SELECT r FROM ReturnRequest r WHERE r.id = :id")
    Optional<ReturnRequest> findWithDetailsByIdForUpdate(@Param("id") Long id);
}
