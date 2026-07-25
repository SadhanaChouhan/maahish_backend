package com.maahish.entity;

import com.maahish.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 2)
    @Builder.Default
    private String countryCode = "IN";

    @Column(nullable = false, length = 100)
    @Builder.Default
    private String country = "India";

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, length = 15)
    private String mobile;

    @Column(length = 15)
    private String alternateMobile;

    @Column(nullable = false, length = 10)
    private String pincode;

    @Column(nullable = false, length = 100)
    private String state;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(length = 100)
    private String district;

    @Column(nullable = false, length = 500)
    private String addressLine;

    @Column(length = 255)
    private String landmark;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isDefault = false;
}
