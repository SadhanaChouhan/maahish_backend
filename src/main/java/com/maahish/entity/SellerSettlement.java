package com.maahish.entity;

import com.maahish.audit.AuditableEntity;
import com.maahish.enums.SettlementStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "seller_settlements", indexes = {
        @Index(name = "idx_settlement_seller", columnList = "seller_id"),
        @Index(name = "idx_settlement_order", columnList = "order_id"),
        @Index(name = "idx_settlement_status", columnList = "settlement_status"),
        @Index(name = "idx_settlement_order_item", columnList = "order_item_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerSettlement extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_item_id", nullable = false, unique = true)
    private OrderItem orderItem;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal grossAmount;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal commissionPercentage;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal commissionAmount;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal netSellerAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "settlement_status", nullable = false, length = 20)
    @Builder.Default
    private SettlementStatus settlementStatus = SettlementStatus.PENDING;

    private LocalDateTime settlementDate;

    @Column(length = 120)
    private String transactionReference;

    @Column(name = "commission_rule_id")
    private Long commissionRuleId;

    @Column(length = 100)
    private String matchedFabric;

    @Column(length = 150)
    private String matchedCategoryName;
}
