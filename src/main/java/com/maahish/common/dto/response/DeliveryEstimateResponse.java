package com.maahish.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryEstimateResponse {

    private String pincode;
    private String city;
    private String state;
    private int estimatedDaysMin;
    private int estimatedDaysMax;
    private String deliveryMessage;
    private boolean serviceable;
}
