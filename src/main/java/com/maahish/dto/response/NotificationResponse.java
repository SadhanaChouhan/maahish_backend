package com.maahish.dto.response;

import com.maahish.enums.NotificationChannel;
import com.maahish.enums.NotificationReferenceType;
import com.maahish.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private Long id;
    private NotificationType type;
    private String title;
    private String message;
    private NotificationReferenceType referenceType;
    private Long referenceId;
    private Boolean read;
    private NotificationChannel channel;
    private LocalDateTime createdAt;
}
