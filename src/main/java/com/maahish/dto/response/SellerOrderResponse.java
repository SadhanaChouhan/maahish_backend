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
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerOrderResponse {

    private Long orderId;
    private String orderNumber;
    private OrderStatus orderStatus;
    private PaymentStatus paymentStatus;
    private LocalDateTime orderDate;
    private LocalDateTime estimatedDeliveryDate;
    private String customerName;
    private BigDecimal sellerSubtotal;
    private BigDecimal orderAmount;
    private BigDecimal commissionAmount;
    private BigDecimal netSellerEarnings;
    private SettlementStatus settlementStatus;
    private Boolean sellerConfirmed;
    private String trackingNumber;
    private String statusNote;
    private String orderNotes;
    private List<SellerOrderItemResponse> items;
}
