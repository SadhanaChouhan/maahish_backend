package com.maahish.catalog.entity;

import com.maahish.common.audit.AuditableEntity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "fabric_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FabricType extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(unique = true, length = 120)
    private String slug;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @OneToMany(mappedBy = "fabricType")
    @Builder.Default
    private List<Product> products = new ArrayList<>();
}
