package com.maahish.controller;

import com.maahish.constants.AppConstants;
import com.maahish.dto.response.ApiResponse;
import com.maahish.dto.response.NotificationResponse;
import com.maahish.dto.response.PageResponse;
import com.maahish.security.SecurityUtil;
import com.maahish.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "In-app notifications for admin, seller, and users")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "List notifications for the current user")
    public ResponseEntity<ApiResponse<PageResponse<NotificationResponse>>> list(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return ResponseEntity.ok(ApiResponse.success(
                notificationService.getNotifications(SecurityUtil.getCurrentUserId(), page, size)));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Unread notification count")
    public ResponseEntity<ApiResponse<Long>> unreadCount() {
        return ResponseEntity.ok(ApiResponse.success(
                notificationService.getUnreadCount(SecurityUtil.getCurrentUserId())));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark a notification as read")
    public ResponseEntity<ApiResponse<Void>> markRead(@PathVariable Long id) {
        notificationService.markAsRead(SecurityUtil.getCurrentUserId(), id);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read"));
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<ApiResponse<Void>> markAllRead() {
        notificationService.markAllAsRead(SecurityUtil.getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read"));
    }
}
