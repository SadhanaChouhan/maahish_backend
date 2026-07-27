package com.maahish.returns.repository;

import com.maahish.returns.entity.RefundTransaction;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefundTransactionRepository extends JpaRepository<RefundTransaction, Long> {

    Optional<RefundTransaction> findByReturnRequestId(Long returnRequestId);
}
