package com.maahish.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerOrderItemResponse {

    private Long orderItemId;
    private Long productId;
    private String productSlug;
    private String productName;
    private String productImageUrl;
    private Integer qty;
    private BigDecimal price;
    private BigDecimal lineTotal;
}
