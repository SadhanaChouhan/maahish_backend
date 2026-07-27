package com.maahish.returns.entity;

import com.maahish.returns.enums.ReturnRequestStatus;
import com.maahish.common.enums.UserRole;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "return_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "return_request_id", nullable = false)
    private ReturnRequest returnRequest;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private ReturnRequestStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReturnRequestStatus toStatus;

    @Column(length = 1000)
    private String remarks;

    @Column(name = "changed_by_user_id")
    private Long changedByUserId;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private UserRole changedByRole;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
