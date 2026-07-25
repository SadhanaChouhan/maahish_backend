package com.maahish.dto.response;

import com.maahish.enums.OrderStatus;
import com.maahish.enums.PaymentStatus;
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
    private LocalDateTime createdAt;
}
