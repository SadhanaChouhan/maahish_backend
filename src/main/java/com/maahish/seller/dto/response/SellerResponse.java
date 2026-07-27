package com.maahish.seller.dto.response;

import com.maahish.seller.enums.SellerStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerResponse {

    private Long id;
    private Long userId;
    private String businessName;
    private String ownerName;
    private String email;
    private String mobile;
    private String businessAddress;
    private String city;
    private String state;
    private String pincode;
    private String gst;
    private String pan;
    private String bankAccountHolder;
    private String bankAccountNumber;
    private String bankIfsc;
    private String bankName;
    private String upiId;
    private String businessLogoUrl;
    private String profileImageUrl;
    private SellerStatus status;
    private String rejectionReason;
    private Boolean platformOwned;
    private Long productCount;
    private Long totalOrders;
    private java.math.BigDecimal totalSales;
    private java.math.BigDecimal totalCommission;
    private java.math.BigDecimal netEarnings;
    private java.math.BigDecimal pendingSettlementAmount;
    private java.math.BigDecimal paidSettlementAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
