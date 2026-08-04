package com.maahish.notification.service;

import com.maahish.infrastructure.mail.service.MailService;
import com.maahish.notification.entity.Notification;
import com.maahish.common.enums.NotificationChannel;
import com.maahish.notification.entity.NotificationPreference;
import com.maahish.notification.repository.NotificationPreferenceRepository;
import com.maahish.notification.enums.NotificationReferenceType;
import com.maahish.notification.repository.NotificationRepository;
import com.maahish.notification.enums.NotificationType;
import com.maahish.user.entity.User;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationDispatcher {

    private static final Set<NotificationType> MANDATORY_TYPES = Set.of(
            NotificationType.SELLER_REGISTRATION_PENDING,
            NotificationType.SELLER_APPROVED,
            NotificationType.SELLER_REJECTED,
            NotificationType.SELLER_SUSPENDED,
            NotificationType.SELLER_ACTIVATED,
            NotificationType.NEW_ORDER,
            NotificationType.ORDER_CONFIRMED,
            NotificationType.ORDER_SHIPPED,
            NotificationType.ORDER_OUT_FOR_DELIVERY,
            NotificationType.ORDER_DELIVERED,
            NotificationType.ORDER_CANCELLED,
            NotificationType.NEW_RETURN_REQUEST,
            NotificationType.RETURN_REQUEST_SUBMITTED,
            NotificationType.RETURN_APPROVED,
            NotificationType.RETURN_REJECTED,
            NotificationType.RETURN_PARCEL_RECEIVED,
            NotificationType.REFUND_PENDING,
            NotificationType.REFUND_COMPLETED,
            NotificationType.EXCHANGE_REQUESTED,
            NotificationType.EXCHANGE_SHIPPED,
            NotificationType.EXCHANGE_COMPLETED,
            NotificationType.CUSTOMER_SHIPPED_RETURN
    );

    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final MailService mailService;

    @Transactional
    public void dispatch(DispatchRequest request) {
        User user = request.user();
        NotificationPreference preferences = getOrCreatePreferences(user);
        boolean mandatory = request.mandatory() || MANDATORY_TYPES.contains(request.type());

        if (request.inApp() && (mandatory || Boolean.TRUE.equals(preferences.getInAppEnabled()))) {
            Notification notification = Notification.builder()
                    .user(user)
                    .type(request.type())
                    .title(request.title())
                    .message(request.message())
                    .referenceType(request.referenceType())
                    .referenceId(request.referenceId())
                    .read(false)
                    .actionPath(request.actionPath())
                    .channel(NotificationChannel.IN_APP)
                    .build();
            notificationRepository.save(notification);
        }

        if (request.email() && StringUtils.hasText(user.getEmail())
                && (mandatory || Boolean.TRUE.equals(preferences.getEmailEnabled()))) {
            mailService.sendNotificationEmail(user.getEmail(), request.title(), request.message());
        }
    }

    @Transactional
    public NotificationPreference getOrCreatePreferences(User user) {
        return preferenceRepository.findByUserId(user.getId())
                .orElseGet(() -> preferenceRepository.save(NotificationPreference.builder()
                        .user(user)
                        .emailEnabled(true)
                        .inAppEnabled(true)
                        .build()));
    }

    @Builder
    public record DispatchRequest(
            User user,
            NotificationType type,
            String title,
            String message,
            NotificationReferenceType referenceType,
            Long referenceId,
            boolean inApp,
            boolean email,
            boolean mandatory,
            String actionPath
    ) {
    }
}
