package com.maahish.notification.service;

import com.maahish.common.exception.ForbiddenException;
import com.maahish.notification.entity.Notification;
import com.maahish.notification.entity.NotificationPreference;
import com.maahish.notification.dto.response.NotificationPreferenceResponse;
import com.maahish.notification.dto.request.NotificationPreferenceUpdateRequest;
import com.maahish.notification.enums.NotificationReferenceType;
import com.maahish.notification.repository.NotificationRepository;
import com.maahish.notification.dto.response.NotificationResponse;
import com.maahish.notification.enums.NotificationType;
import com.maahish.common.util.PageMapper;
import com.maahish.common.dto.response.PageResponse;
import com.maahish.common.util.PaginationUtil;
import com.maahish.common.exception.ResourceNotFoundException;
import com.maahish.seller.entity.Seller;
import com.maahish.user.entity.User;
import com.maahish.user.repository.UserRepository;
import com.maahish.common.enums.UserRole;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationDispatcher notificationDispatcher;

    @Override
    @Transactional
    public void notifyUser(User user, NotificationType type, String title, String message,
                           NotificationReferenceType referenceType, Long referenceId, boolean sendEmail) {
        notificationDispatcher.dispatch(NotificationDispatcher.DispatchRequest.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .inApp(true)
                .email(sendEmail)
                .sms(false)
                .smsMobile(user.getMobile())
                .mandatory(false)
                .actionPath(null)
                .build());
    }

    @Override
    @Transactional
    public void notifyAdmins(NotificationType type, String title, String message,
                             NotificationReferenceType referenceType, Long referenceId) {
        List<User> admins = userRepository.findByRole(UserRole.ROLE_ADMIN);
        for (User admin : admins) {
            notifyUser(admin, type, title, message, referenceType, referenceId, false);
        }
    }

    @Override
    @Transactional
    public void notifySeller(Seller seller, NotificationType type, String title, String message,
                             NotificationReferenceType referenceType, Long referenceId, boolean sendEmail) {
        notifyUser(seller.getUser(), type, title, message, referenceType, referenceId, sendEmail);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> getNotifications(Long userId, int page, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Page<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(
                user, PaginationUtil.createPageable(page, size, "createdAt", "desc"));
        return PageMapper.toPageResponse(notifications, this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return notificationRepository.countByUserAndReadFalse(user);
    }

    @Override
    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        if (!notification.getUser().getId().equals(userId)) {
            throw new ForbiddenException("Access denied");
        }
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllReadForUser(userId);
    }

    @Override
    @Transactional
    public void deleteNotification(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        if (!notification.getUser().getId().equals(userId)) {
            throw new ForbiddenException("Access denied");
        }
        notificationRepository.delete(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationPreferenceResponse getPreferences(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        NotificationPreference preferences = notificationDispatcher.getOrCreatePreferences(user);
        return toPreferenceResponse(preferences);
    }

    @Override
    @Transactional
    public NotificationPreferenceResponse updatePreferences(Long userId,
                                                            NotificationPreferenceUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        NotificationPreference preferences = notificationDispatcher.getOrCreatePreferences(user);
        preferences.setEmailEnabled(request.getEmailEnabled());
        preferences.setSmsEnabled(request.getSmsEnabled());
        preferences.setInAppEnabled(request.getInAppEnabled());
        return toPreferenceResponse(preferences);
    }

    private NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .referenceType(notification.getReferenceType())
                .referenceId(notification.getReferenceId())
                .read(notification.getRead())
                .channel(notification.getChannel())
                .actionPath(notification.getActionPath())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    private NotificationPreferenceResponse toPreferenceResponse(NotificationPreference preferences) {
        return NotificationPreferenceResponse.builder()
                .emailEnabled(preferences.getEmailEnabled())
                .smsEnabled(preferences.getSmsEnabled())
                .inAppEnabled(preferences.getInAppEnabled())
                .build();
    }
}
