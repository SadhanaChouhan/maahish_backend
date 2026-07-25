package com.maahish.service;

import com.maahish.dto.response.NotificationResponse;
import com.maahish.dto.response.PageResponse;
import com.maahish.entity.Seller;
import com.maahish.entity.User;
import com.maahish.enums.NotificationReferenceType;
import com.maahish.enums.NotificationType;

public interface NotificationService {

    void notifyUser(User user, NotificationType type, String title, String message,
                    NotificationReferenceType referenceType, Long referenceId, boolean sendEmail);

    void notifyAdmins(NotificationType type, String title, String message,
                      NotificationReferenceType referenceType, Long referenceId);

    void notifySeller(Seller seller, NotificationType type, String title, String message,
                      NotificationReferenceType referenceType, Long referenceId, boolean sendEmail);

    PageResponse<NotificationResponse> getNotifications(Long userId, int page, int size);

    long getUnreadCount(Long userId);

    void markAsRead(Long userId, Long notificationId);

    void markAllAsRead(Long userId);
}
