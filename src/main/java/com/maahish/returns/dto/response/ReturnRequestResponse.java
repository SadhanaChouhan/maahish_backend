package com.maahish.returns.dto.response;

import com.maahish.returns.enums.PreferredResolution;
import com.maahish.returns.enums.ReturnReason;
import com.maahish.returns.enums.ReturnRequestStatus;
import com.maahish.returns.enums.ReturnType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnRequestResponse {

    private Long id;
    private String returnNumber;
    private Long orderId;
    private String orderNumber;
    private Long orderItemId;
    private String productName;
    private String productImageUrl;
    private Integer qty;
    private BigDecimal lineTotal;
    private Long sellerId;
    private String sellerBusinessName;
    private ReturnType returnType;
    private ReturnReason reason;
    private String description;
    private PreferredResolution preferredResolution;
    private ReturnRequestStatus status;
    private String adminRemarks;
    private String rejectionReason;
    private String customerRemarks;
    private BigDecimal refundAmount;
    private String exchangeTrackingNumber;
    private LocalDateTime deliveredAtSnapshot;
    private List<ReturnImageResponse> images;
    private ReturnShipmentResponse shipment;
    private RefundTransactionResponse refundTransaction;
    private List<ReturnHistoryResponse> history;
    private ReturnEligibilityResponse eligibility;
    private String returnInstructions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
