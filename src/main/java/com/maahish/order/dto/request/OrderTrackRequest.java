package com.maahish.order.dto.request;

import com.maahish.order.entity.Order;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrderTrackRequest {

    @NotBlank(message = "Order number is required")
    private String orderNumber;

    /** Email or mobile used when placing the order (required to verify ownership). */
    @NotBlank(message = "Email or mobile is required")
    private String contact;
}
