package com.maahish.seller.dto.response;

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
public class SellerSettlementResponse {

    private Long id;
    private Long sellerId;
    private String sellerBusinessName;
    private String sellerOwnerName;
    private Long orderId;
    private String orderNumber;
    private Long orderItemId;
    private String productName;
    private BigDecimal grossAmount;
    private BigDecimal commissionPercentage;
    private BigDecimal commissionAmount;
    private BigDecimal netSellerAmount;
    private SettlementStatus settlementStatus;
    private LocalDateTime settlementDate;
    private String transactionReference;
    private String matchedFabric;
    private String matchedCategoryName;
    private LocalDateTime createdAt;
}
