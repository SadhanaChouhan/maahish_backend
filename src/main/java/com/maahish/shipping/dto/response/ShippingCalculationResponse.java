package com.maahish.shipping.dto.response;

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
public class ShippingCalculationResponse {

    private List<CheckoutSummaryItemResponse> items;
    private BigDecimal subtotal;
    private BigDecimal originalShippingCharge;
    private BigDecimal shippingCharge;
    private BigDecimal shippingDiscount;
    private BigDecimal total;
    private boolean freeShipping;
    private String appliedRuleName;
    private String appliedRuleType;
    private String shippingMessage;
    private String upsellMessage;
}
