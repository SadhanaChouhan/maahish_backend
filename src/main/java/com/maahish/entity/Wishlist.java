package com.maahish.entity;

import com.maahish.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "wishlists", uniqueConstraints = {
        @UniqueConstraint(name = "uk_wishlist_user_product", columnNames = {"user_id", "product_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wishlist extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}
