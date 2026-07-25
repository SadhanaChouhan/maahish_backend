package com.maahish.repository;

import com.maahish.entity.PendingCheckout;
import com.maahish.enums.PendingCheckoutStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PendingCheckoutRepository extends JpaRepository<PendingCheckout, Long> {

    @EntityGraph(attributePaths = {"user", "address", "items", "items.product"})
    Optional<PendingCheckout> findWithDetailsByRazorpayOrderId(String razorpayOrderId);

    List<PendingCheckout> findByUserIdAndStatus(Long userId, PendingCheckoutStatus status);

    Optional<PendingCheckout> findByCheckoutReferenceAndUserId(String checkoutReference, Long userId);
}
