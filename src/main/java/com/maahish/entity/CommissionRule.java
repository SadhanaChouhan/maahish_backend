package com.maahish.entity;

import com.maahish.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "commission_rules", indexes = {
        @Index(name = "idx_commission_fabric", columnList = "fabric"),
        @Index(name = "idx_commission_category", columnList = "category_id"),
        @Index(name = "idx_commission_enabled", columnList = "enabled")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommissionRule extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 100)
    private String fabric;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal commissionPercentage;

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;
}
