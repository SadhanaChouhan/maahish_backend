package com.maahish.service.impl;

import com.maahish.dto.response.NotificationResponse;
import com.maahish.dto.response.PageResponse;
import com.maahish.entity.Notification;
import com.maahish.entity.Seller;
import com.maahish.entity.User;
import com.maahish.enums.NotificationChannel;
import com.maahish.enums.NotificationReferenceType;
import com.maahish.enums.NotificationType;
import com.maahish.enums.UserRole;
import com.maahish.exception.ForbiddenException;
import com.maahish.exception.ResourceNotFoundException;
import com.maahish.mail.MailService;
import com.maahish.repository.NotificationRepository;
import com.maahish.repository.UserRepository;
import com.maahish.service.NotificationService;
import com.maahish.util.PageMapper;
import com.maahish.util.PaginationUtil;
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
    private final MailService mailService;

    @Override
    @Transactional
    public void notifyUser(User user, NotificationType type, String title, String message,
                           NotificationReferenceType referenceType, Long referenceId, boolean sendEmail) {
        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .read(false)
                .channel(NotificationChannel.IN_APP)
                .build();
        notificationRepository.save(notification);

        if (sendEmail) {
            mailService.sendNotificationEmail(user.getEmail(), title, message);
        }
        log.debug("In-app notification created for user {}: {}", user.getEmail(), type);
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
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
