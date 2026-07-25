package com.maahish.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CommissionRuleRequest {

    @NotBlank(message = "Rule name is required")
    private String name;

    private String fabric;

    private Long categoryId;

    @NotNull(message = "Commission percentage is required")
    @DecimalMin(value = "0", message = "Commission cannot be negative")
    @DecimalMax(value = "100", message = "Commission cannot exceed 100%")
    private BigDecimal commissionPercentage;

    private Boolean enabled;
}
