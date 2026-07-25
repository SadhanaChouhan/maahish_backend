package com.maahish.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AddressRequest {

    @NotBlank(message = "Country is required")
    private String countryCode = "IN";

    private String country = "India";

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Mobile is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid mobile number")
    private String mobile;

    @Pattern(regexp = "^$|^[6-9]\\d{9}$", message = "Invalid alternate mobile number")
    private String alternateMobile;

    @NotBlank(message = "PIN code is required")
    @Pattern(regexp = "^[1-9][0-9]{5}$", message = "Invalid PIN code")
    private String pincode;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "City is required")
    private String city;

    private String district;

    @NotBlank(message = "Address line is required")
    private String addressLine;

    private String landmark;
    private Boolean isDefault;
}
