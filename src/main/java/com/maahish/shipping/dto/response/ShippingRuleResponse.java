package com.maahish.shipping.dto.response;

import com.maahish.shipping.enums.ShippingRuleType;
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
public class ShippingRuleResponse {

    private Long id;
    private String ruleName;
    private ShippingRuleType ruleType;
    private BigDecimal shippingCharge;
    private BigDecimal minimumOrderAmount;
    private Boolean isFirstOrderOnly;
    private String state;
    private String city;
    private String pincode;
    private Integer priority;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
