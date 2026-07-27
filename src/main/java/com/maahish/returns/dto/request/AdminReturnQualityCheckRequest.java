package com.maahish.returns.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminReturnQualityCheckRequest {

    @NotNull
    private Boolean passed;

    @Size(max = 1000)
    private String adminRemarks;
}
