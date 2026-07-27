package com.maahish.notification.service;

import com.maahish.notification.dto.response.NotificationPreferenceResponse;
import com.maahish.notification.dto.request.NotificationPreferenceUpdateRequest;
import com.maahish.notification.enums.NotificationReferenceType;
import com.maahish.notification.dto.response.NotificationResponse;
import com.maahish.notification.enums.NotificationType;
import com.maahish.common.dto.response.PageResponse;
import com.maahish.seller.entity.Seller;
import com.maahish.user.entity.User;


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

    void deleteNotification(Long userId, Long notificationId);

    NotificationPreferenceResponse getPreferences(Long userId);

    NotificationPreferenceResponse updatePreferences(Long userId, NotificationPreferenceUpdateRequest request);
}
