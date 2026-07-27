package com.maahish.seller.dto.request;

import com.maahish.seller.enums.SellerStatus;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SellerStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private SellerStatus status;

    @Size(max = 500)
    private String rejectionReason;
}
