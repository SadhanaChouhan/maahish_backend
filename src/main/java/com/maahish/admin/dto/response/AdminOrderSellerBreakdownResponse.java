package com.maahish.admin.dto.response;

import com.maahish.settlement.enums.SettlementStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminOrderSellerBreakdownResponse {

    private Long sellerId;
    private String sellerBusinessName;
    private String sellerOwnerName;
    private Boolean platformOwned;
    private Boolean sellerConfirmed;
    private List<AdminOrderBreakdownItemResponse> items;
    private BigDecimal productTotal;
    private BigDecimal commissionPercentage;
    private BigDecimal commissionAmount;
    private BigDecimal netSellerAmount;
    private SettlementStatus settlementStatus;
}
