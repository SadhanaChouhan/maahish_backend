package com.maahish.returns.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminReturnReviewRequest {

    @NotNull
    private Boolean approved;

    @Size(max = 1000)
    private String adminRemarks;

    @Size(max = 1000)
    private String rejectionReason;
}
