package com.maahish.shipping.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutSummaryItemResponse {

    private Long productId;
    private String productName;
    private String sellerName;
    private String imageUrl;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
}
