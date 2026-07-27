package com.maahish.notification.entity;

import com.maahish.common.audit.AuditableEntity;
import com.maahish.user.entity.User;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notification_preferences", indexes = {
        @Index(name = "idx_notification_pref_user", columnList = "user_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreference extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "email_enabled", nullable = false)
    @Builder.Default
    private Boolean emailEnabled = true;

    @Column(name = "sms_enabled", nullable = false)
    @Builder.Default
    private Boolean smsEnabled = true;

    @Column(name = "in_app_enabled", nullable = false)
    @Builder.Default
    private Boolean inAppEnabled = true;
}
