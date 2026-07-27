package com.maahish.returns.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "return_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "return_request_id", nullable = false)
    private ReturnRequest returnRequest;

    @Column(nullable = false, length = 500)
    private String imageUrl;

    @Column(length = 255)
    private String publicId;

    @Column(nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;
}
