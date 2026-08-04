package com.maahish.shipping.entity;

import com.maahish.common.audit.AuditableEntity;
import com.maahish.shipping.enums.ShippingRuleType;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "shipping_rules", indexes = {
        @Index(name = "idx_shipping_rule_type", columnList = "ruleType"),
        @Index(name = "idx_shipping_rule_active", columnList = "active"),
        @Index(name = "idx_shipping_rule_priority", columnList = "priority")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingRule extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_name", nullable = false, length = 150)
    private String ruleName;

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type", nullable = false, length = 40)
    private ShippingRuleType ruleType;

    @Column(name = "shipping_charge", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal shippingCharge = BigDecimal.ZERO;

    @Column(name = "minimum_order_amount", precision = 12, scale = 2)
    private BigDecimal minimumOrderAmount;

    @Column(name = "is_first_order_only", nullable = false)
    @Builder.Default
    private Boolean isFirstOrderOnly = false;

    @Column(length = 100)
    private String state;

    @Column(length = 100)
    private String city;

    @Column(length = 10)
    private String pincode;

    @Column(nullable = false)
    @Builder.Default
    private Integer priority = 100;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
}
