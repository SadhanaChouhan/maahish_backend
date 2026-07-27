package com.maahish.order.dto.response;

import com.maahish.user.dto.response.AddressResponse;
import com.maahish.user.dto.response.CustomerSummaryResponse;
import com.maahish.order.enums.OrderStatus;
import com.maahish.payment.dto.response.PaymentResponse;
import com.maahish.payment.enums.PaymentStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private Long id;
    private String orderNumber;
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private BigDecimal subtotal;
    private BigDecimal shippingCharge;
    private BigDecimal total;
    private String trackingNumber;
    private String statusNote;
    private String orderNotes;
    private CustomerSummaryResponse customer;
    private AddressResponse address;
    private List<OrderItemResponse> items;
    private PaymentResponse payment;
    private LocalDateTime deliveredAt;
    private LocalDateTime createdAt;
}
