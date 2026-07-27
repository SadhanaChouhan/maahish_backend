package com.maahish.seller.dto.response;

import com.maahish.order.enums.OrderStatus;
import com.maahish.payment.enums.PaymentStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerOrderSummaryResponse {

    private Long orderId;
    private String orderNumber;
    private OrderStatus orderStatus;
    private PaymentStatus paymentStatus;
    private LocalDateTime orderDate;
    private String customerName;
    private BigDecimal sellerAmount;
    private int itemCount;
}
