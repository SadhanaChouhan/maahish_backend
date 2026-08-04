package com.maahish.payment.dto.response;

import com.maahish.payment.enums.PaymentMethod;
import com.maahish.payment.enums.PaymentStatus;





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
public class PaymentResponse {



    private Long id;

    private String transactionId;

    private String razorpayOrderId;

    private BigDecimal amount;

    private PaymentStatus status;

    private PaymentMethod method;

    private String razorpayKeyId;

    private Integer amountPaise;

    private String currency;

    private String customerName;

    private String customerEmail;

    private String customerPhone;

    private String checkoutReference;

    private LocalDateTime expiresAt;

}

