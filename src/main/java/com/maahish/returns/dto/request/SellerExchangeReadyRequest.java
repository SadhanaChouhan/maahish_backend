package com.maahish.returns.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SellerExchangeReadyRequest {

    @Size(max = 1000)
    private String sellerRemarks;
}
