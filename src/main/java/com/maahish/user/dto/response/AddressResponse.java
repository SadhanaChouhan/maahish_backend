package com.maahish.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {

    private Long id;
    private String countryCode;
    private String country;
    private String fullName;
    private String mobile;
    private String alternateMobile;
    private String pincode;
    private String state;
    private String city;
    private String district;
    private String addressLine;
    private String landmark;
    private Boolean isDefault;
}
