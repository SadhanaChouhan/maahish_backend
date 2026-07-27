package com.maahish.returns.entity;

import com.maahish.common.audit.AuditableEntity;
import com.maahish.returns.enums.CourierCompany;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "return_shipments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnShipment extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "return_request_id", nullable = false, unique = true)
    private ReturnRequest returnRequest;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CourierCompany courierCompany;

    @Column(nullable = false, length = 100)
    private String trackingNumber;

    @Column(nullable = false)
    private LocalDate dispatchDate;

    @Column(length = 500)
    private String receiptUrl;

    @Column(length = 255)
    private String receiptPublicId;
}
