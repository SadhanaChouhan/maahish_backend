package com.maahish.returns.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnEligibilityResponse {

    private boolean eligible;
    private String reason;
    private Long existingReturnId;
    private String existingReturnNumber;
    private Long daysRemaining;
    private boolean canReturn;
    private boolean canExchange;
}
