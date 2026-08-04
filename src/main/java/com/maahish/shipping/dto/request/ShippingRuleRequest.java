package com.maahish.shipping.dto.request;

import com.maahish.shipping.enums.ShippingRuleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ShippingRuleRequest {

    @NotBlank
    private String ruleName;

    @NotNull
    private ShippingRuleType ruleType;

    @NotNull
    private BigDecimal shippingCharge;

    private BigDecimal minimumOrderAmount;

    private Boolean isFirstOrderOnly;

    private String state;

    private String city;

    private String pincode;

    @NotNull
    private Integer priority;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    @NotNull
    private Boolean active;
}
