package com.maahish.seller.entity;

import com.maahish.common.audit.AuditableEntity;
import com.maahish.catalog.entity.Product;
import com.maahish.seller.enums.SellerStatus;
import com.maahish.user.entity.User;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sellers", indexes = {
        @Index(name = "idx_seller_email", columnList = "email", unique = true),
        @Index(name = "idx_seller_status", columnList = "status"),
        @Index(name = "idx_seller_user", columnList = "user_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seller extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, length = 150)
    private String businessName;

    @Column(nullable = false, length = 100)
    private String ownerName;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false, length = 15)
    private String mobile;

    @Column(nullable = false, length = 500)
    private String businessAddress;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 100)
    private String state;

    @Column(nullable = false, length = 10)
    private String pincode;

    @Column(length = 20)
    private String gst;

    @Column(length = 15)
    private String pan;

    @Column(nullable = false, length = 120)
    private String bankAccountHolder;

    @Column(nullable = false, length = 30)
    private String bankAccountNumber;

    @Column(nullable = false, length = 15)
    private String bankIfsc;

    @Column(nullable = false, length = 120)
    private String bankName;

    @Column(length = 100)
    private String upiId;

    @Column(length = 500)
    private String businessLogoUrl;

    @Column(length = 255)
    private String businessLogoPublicId;

    @Column(length = 500)
    private String profileImageUrl;

    @Column(length = 255)
    private String profileImagePublicId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SellerStatus status = SellerStatus.PENDING;

    @Column(length = 500)
    private String rejectionReason;

    @Column(nullable = false)
    @Builder.Default
    private Boolean platformOwned = false;

    @OneToMany(mappedBy = "seller", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Product> products = new ArrayList<>();
}
