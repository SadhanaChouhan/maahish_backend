package com.maahish.entity;

import com.maahish.audit.AuditableEntity;
import com.maahish.enums.PendingCheckoutStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pending_checkouts", indexes = {
        @Index(name = "idx_pending_checkout_ref", columnList = "checkoutReference", unique = true),
        @Index(name = "idx_pending_checkout_razorpay", columnList = "razorpayOrderId", unique = true),
        @Index(name = "idx_pending_checkout_user", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingCheckout extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;

    @Column(length = 500)
    private String orderNotes;

    @Column(nullable = false, unique = true, length = 40)
    private String checkoutReference;

    @Column(name = "razorpay_order_id", unique = true, length = 100)
    private String razorpayOrderId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal shippingCharge = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PendingCheckoutStatus status = PendingCheckoutStatus.PENDING;

    @Column(length = 50)
    private String orderNumber;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    @Builder.Default
    private Boolean buyNow = false;

    @OneToMany(mappedBy = "pendingCheckout", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PendingCheckoutItem> items = new ArrayList<>();
}
