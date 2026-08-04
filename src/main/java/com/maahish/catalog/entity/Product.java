package com.maahish.catalog.entity;

import com.maahish.common.audit.AuditableEntity;
import com.maahish.catalog.enums.ProductStatus;
import com.maahish.seller.entity.Seller;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_product_code", columnList = "productCode", unique = true),
        @Index(name = "idx_product_slug", columnList = "slug", unique = true),
        @Index(name = "idx_product_category", columnList = "category_id"),
        @Index(name = "idx_product_status", columnList = "status"),
        @Index(name = "idx_product_seller", columnList = "seller_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String productCode;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(unique = true, length = 300)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal sellingPrice;

    @Column(nullable = false)
    @Builder.Default
    private Integer stock = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer reservedStock = 0;

    @Column(length = 100)
    @Builder.Default
    private String brand = "Maahish";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private Seller seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fabric_type_id")
    private FabricType fabricType;

    @Column(length = 50)
    private String color;

    @Column(length = 100)
    private String occasion;

    @Column(precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal rating = BigDecimal.ZERO;

    @Column
    @Builder.Default
    private Integer reviewCount = 0;

    @Column(length = 500)
    private String videoUrl;

    @Column(length = 500)
    private String image360Url;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ProductStatus status = ProductStatus.ACTIVE;

    @Column(nullable = false)
    @Builder.Default
    private Boolean bestSeller = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean latestArrival = false;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Review> reviews = new ArrayList<>();
}
