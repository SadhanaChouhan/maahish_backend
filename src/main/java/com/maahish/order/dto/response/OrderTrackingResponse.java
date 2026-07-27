package com.maahish.order.dto.response;

import com.maahish.order.enums.OrderStatus;
import com.maahish.payment.enums.PaymentStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderTrackingResponse {

    private String orderNumber;
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private String trackingNumber;
    private String statusNote;
    private LocalDateTime createdAt;
    private List<OrderTrackingItemResponse> items;
}
