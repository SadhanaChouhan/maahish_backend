package com.maahish.returns.dto.response;

import com.maahish.returns.enums.CourierCompany;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnShipmentResponse {

    private Long id;
    private CourierCompany courierCompany;
    private String trackingNumber;
    private LocalDate dispatchDate;
    private String receiptUrl;
    private LocalDateTime createdAt;
}
