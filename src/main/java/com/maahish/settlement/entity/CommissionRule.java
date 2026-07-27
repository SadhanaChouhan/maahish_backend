package com.maahish.settlement.entity;

import com.maahish.common.audit.AuditableEntity;
import com.maahish.catalog.entity.Category;
import com.maahish.catalog.entity.FabricType;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "commission_rules", indexes = {
        @Index(name = "idx_commission_fabric_type", columnList = "fabric_type_id"),
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fabric_type_id")
    private FabricType fabricType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal commissionPercentage;

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;
}
