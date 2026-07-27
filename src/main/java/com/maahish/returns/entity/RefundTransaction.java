package com.maahish.returns.entity;

import com.maahish.common.audit.AuditableEntity;
import com.maahish.returns.enums.RefundTransactionStatus;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "refund_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundTransaction extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "return_request_id", nullable = false)
    private ReturnRequest returnRequest;

    @Column(length = 100)
    private String razorpayRefundId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private RefundTransactionStatus status = RefundTransactionStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String rawResponse;
}
