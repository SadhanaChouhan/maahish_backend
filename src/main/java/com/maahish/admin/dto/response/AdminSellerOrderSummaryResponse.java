package com.maahish.admin.dto.response;

import com.maahish.order.enums.OrderStatus;
import com.maahish.payment.enums.PaymentStatus;
import com.maahish.settlement.enums.SettlementStatus;

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
public class AdminSellerOrderSummaryResponse {

    private Long orderId;
    private String orderNumber;
    private LocalDateTime orderDate;
    private OrderStatus orderStatus;
    private PaymentStatus paymentStatus;
    private String customerName;
    private Integer itemCount;
    private BigDecimal sellerProductTotal;
    private BigDecimal commissionAmount;
    private BigDecimal netSellerAmount;
    private SettlementStatus settlementStatus;
}
