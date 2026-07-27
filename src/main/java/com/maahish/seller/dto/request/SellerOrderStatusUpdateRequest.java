package com.maahish.seller.dto.request;

import com.maahish.order.enums.OrderStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SellerOrderStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private OrderStatus status;

    private String trackingNumber;

    private String note;
}
