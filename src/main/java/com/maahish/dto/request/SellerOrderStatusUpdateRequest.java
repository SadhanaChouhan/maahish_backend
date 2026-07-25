package com.maahish.dto.request;

import com.maahish.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SellerOrderStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private OrderStatus status;

    private String trackingNumber;

    private String note;
}
