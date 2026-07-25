package com.maahish.dto.response;

import com.maahish.enums.SettlementStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminOrderBreakdownItemResponse {

    private Long settlementId;
    private Long orderItemId;
    private Long productId;
    private String productSlug;
    private String productName;
    private String productImageUrl;
    private Integer qty;
    private BigDecimal price;
    private BigDecimal lineTotal;
    private BigDecimal commissionPercentage;
    private BigDecimal commissionAmount;
    private BigDecimal netSellerAmount;
    private SettlementStatus settlementStatus;
}
