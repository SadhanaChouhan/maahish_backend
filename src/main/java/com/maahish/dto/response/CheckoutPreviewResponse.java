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
public class CheckoutPreviewResponse {

    private Long addressId;
    private BigDecimal subtotal;
    private BigDecimal shippingCharge;
    private BigDecimal total;
    private String message;
}
