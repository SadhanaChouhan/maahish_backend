package com.maahish.returns.dto.request;

import com.maahish.returns.enums.CourierCompany;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ReturnShipmentRequest {

    @NotNull
    private CourierCompany courierCompany;

    @NotBlank
    private String trackingNumber;

    @NotNull
    private LocalDate dispatchDate;
}
