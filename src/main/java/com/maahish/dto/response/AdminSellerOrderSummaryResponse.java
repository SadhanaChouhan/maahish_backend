package com.maahish.dto.response;

import com.maahish.enums.OrderStatus;
import com.maahish.enums.PaymentStatus;
import com.maahish.enums.SettlementStatus;
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
