package com.maahish.returns.dto.response;

import com.maahish.returns.enums.RefundTransactionStatus;

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
public class RefundTransactionResponse {

    private Long id;
    private String razorpayRefundId;
    private BigDecimal amount;
    private RefundTransactionStatus status;
    private LocalDateTime createdAt;
}
