package com.maahish.order.entity;

import com.maahish.catalog.entity.Product;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "pending_checkout_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingCheckoutItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pending_checkout_id", nullable = false)
    private PendingCheckout pendingCheckout;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer qty;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, length = 255)
    private String productName;

    @Column(length = 500)
    private String productImageUrl;
}
