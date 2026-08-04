package com.maahish.order.dto.response;

import com.maahish.shipping.dto.response.CheckoutSummaryItemResponse;
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
public class CheckoutPreviewResponse {

    private Long addressId;
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
    private String message;
}
