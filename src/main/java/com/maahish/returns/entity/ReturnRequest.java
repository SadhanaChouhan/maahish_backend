package com.maahish.returns.entity;

import com.maahish.common.audit.AuditableEntity;
import com.maahish.order.entity.Order;
import com.maahish.order.entity.OrderItem;
import com.maahish.returns.enums.PreferredResolution;
import com.maahish.returns.enums.ReturnReason;
import com.maahish.returns.enums.ReturnRequestStatus;
import com.maahish.returns.enums.ReturnType;
import com.maahish.seller.entity.Seller;
import com.maahish.user.entity.User;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "return_requests", indexes = {
        @Index(name = "idx_return_number", columnList = "returnNumber", unique = true),
        @Index(name = "idx_return_order_item", columnList = "order_item_id", unique = true),
        @Index(name = "idx_return_customer", columnList = "customer_id"),
        @Index(name = "idx_return_seller", columnList = "seller_id"),
        @Index(name = "idx_return_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnRequest extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String returnNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_item_id", nullable = false, unique = true)
    private OrderItem orderItem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReturnType returnType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReturnReason reason;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PreferredResolution preferredResolution;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private ReturnRequestStatus status = ReturnRequestStatus.RETURN_REQUESTED;

    @Column(length = 1000)
    private String adminRemarks;

    @Column(length = 1000)
    private String rejectionReason;

    @Column(length = 1000)
    private String customerRemarks;

    @Column(precision = 12, scale = 2)
    private BigDecimal refundAmount;

    @Column(length = 100)
    private String exchangeTrackingNumber;

    @Column(nullable = false)
    private LocalDateTime deliveredAtSnapshot;

    @Builder.Default
    @Column(nullable = false)
    private boolean shipReminderSent = false;

    @OneToMany(mappedBy = "returnRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReturnImage> images = new ArrayList<>();

    @OneToOne(mappedBy = "returnRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private ReturnShipment shipment;

    @OneToMany(mappedBy = "returnRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReturnHistory> history = new ArrayList<>();
}
