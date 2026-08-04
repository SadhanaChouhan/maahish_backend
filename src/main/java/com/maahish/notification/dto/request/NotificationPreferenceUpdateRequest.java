package com.maahish.notification.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NotificationPreferenceUpdateRequest {

    @NotNull
    private Boolean emailEnabled;

    @NotNull
    private Boolean inAppEnabled;
}
