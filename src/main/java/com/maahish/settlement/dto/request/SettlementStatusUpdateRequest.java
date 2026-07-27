package com.maahish.settlement.dto.request;

import com.maahish.settlement.enums.SettlementStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SettlementStatusUpdateRequest {

    @NotNull(message = "Settlement status is required")
    private SettlementStatus status;

    private String transactionReference;
}
