package com.maahish.order.repository;

import com.maahish.order.entity.PendingCheckout;
import com.maahish.order.enums.PendingCheckoutStatus;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PendingCheckoutRepository extends JpaRepository<PendingCheckout, Long> {

    @EntityGraph(attributePaths = {"user", "address", "items", "items.product"})
    Optional<PendingCheckout> findWithDetailsByRazorpayOrderId(String razorpayOrderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"user", "address", "items", "items.product"})
    @Query("SELECT p FROM PendingCheckout p WHERE p.razorpayOrderId = :razorpayOrderId")
    Optional<PendingCheckout> findWithDetailsByRazorpayOrderIdForUpdate(
            @Param("razorpayOrderId") String razorpayOrderId);

    List<PendingCheckout> findByUserIdAndStatus(Long userId, PendingCheckoutStatus status);

    Optional<PendingCheckout> findByCheckoutReferenceAndUserId(String checkoutReference, Long userId);

    @EntityGraph(attributePaths = {"user", "address", "items", "items.product"})
    Optional<PendingCheckout> findWithDetailsByCheckoutReferenceAndUserId(String checkoutReference, Long userId);

    @EntityGraph(attributePaths = {"items", "items.product"})
    List<PendingCheckout> findByStatusAndExpiresAtBefore(PendingCheckoutStatus status, LocalDateTime expiresAt);
}
