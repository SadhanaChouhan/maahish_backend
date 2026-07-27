package com.maahish.notification.controller;

import com.maahish.common.dto.response.ApiResponse;
import com.maahish.common.constants.AppConstants;
import com.maahish.notification.entity.Notification;
import com.maahish.notification.dto.response.NotificationPreferenceResponse;
import com.maahish.notification.dto.request.NotificationPreferenceUpdateRequest;
import com.maahish.notification.dto.response.NotificationResponse;
import com.maahish.notification.service.NotificationService;
import com.maahish.common.dto.response.PageResponse;
import com.maahish.common.security.SecurityUtil;











import io.swagger.v3.oas.annotations.Operation;

import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

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



    @GetMapping("/preferences")

    @Operation(summary = "Get notification preferences for the current user")

    public ResponseEntity<ApiResponse<NotificationPreferenceResponse>> getPreferences() {

        return ResponseEntity.ok(ApiResponse.success(

                notificationService.getPreferences(SecurityUtil.getCurrentUserId())));

    }



    @PutMapping("/preferences")

    @Operation(summary = "Update notification preferences")

    public ResponseEntity<ApiResponse<NotificationPreferenceResponse>> updatePreferences(

            @Valid @RequestBody NotificationPreferenceUpdateRequest request) {

        return ResponseEntity.ok(ApiResponse.success(

                "Preferences updated",

                notificationService.updatePreferences(SecurityUtil.getCurrentUserId(), request)));

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



    @DeleteMapping("/{id}")

    @Operation(summary = "Delete a notification")

    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {

        notificationService.deleteNotification(SecurityUtil.getCurrentUserId(), id);

        return ResponseEntity.ok(ApiResponse.success("Notification deleted"));

    }

}


