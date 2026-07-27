package com.maahish.returns.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminExchangeShipRequest {

    @NotBlank
    private String trackingNumber;

    @Size(max = 1000)
    private String adminRemarks;
}
