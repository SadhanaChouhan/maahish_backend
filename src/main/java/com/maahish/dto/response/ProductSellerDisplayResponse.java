package com.maahish.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSellerDisplayResponse {

    private String businessName;
    private String ownerName;
    private String city;
    private String state;
    private String businessLogoUrl;
    private Boolean platformOwned;
}
