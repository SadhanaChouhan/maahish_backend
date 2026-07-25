package com.maahish.repository;

import com.maahish.entity.OrderSellerAcknowledgement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderSellerAcknowledgementRepository extends JpaRepository<OrderSellerAcknowledgement, Long> {

    boolean existsByOrderIdAndSellerId(Long orderId, Long sellerId);
}
