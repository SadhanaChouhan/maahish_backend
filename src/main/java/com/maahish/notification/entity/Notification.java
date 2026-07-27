package com.maahish.notification.entity;

import com.maahish.common.audit.AuditableEntity;
import com.maahish.common.enums.NotificationChannel;
import com.maahish.notification.enums.NotificationReferenceType;
import com.maahish.notification.enums.NotificationType;
import com.maahish.user.entity.User;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notification_user", columnList = "user_id"),
        @Index(name = "idx_notification_read", columnList = "read_flag")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private NotificationType type;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private NotificationReferenceType referenceType;

    @Column
    private Long referenceId;

    @Column(name = "read_flag", nullable = false)
    @Builder.Default
    private Boolean read = false;

    @Column(name = "action_path", length = 255)
    private String actionPath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private NotificationChannel channel = NotificationChannel.IN_APP;
}
