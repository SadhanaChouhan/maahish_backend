package com.maahish.payment.entity;

import com.maahish.common.audit.AuditableEntity;
import com.maahish.order.entity.Order;
import com.maahish.payment.enums.PaymentMethod;
import com.maahish.payment.enums.PaymentStatus;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_payment_transaction", columnList = "transactionId"),
        @Index(name = "idx_payment_razorpay_order", columnList = "razorpayOrderId")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Payment extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Column(length = 100)
    private String transactionId;

    @Column(name = "razorpay_order_id", length = 100)
    private String razorpayOrderId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PaymentMethod method = PaymentMethod.RAZORPAY;

    @Column(columnDefinition = "TEXT")
    private String rawResponse;
}
