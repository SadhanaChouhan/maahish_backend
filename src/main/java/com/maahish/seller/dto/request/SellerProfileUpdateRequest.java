package com.maahish.seller.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SellerProfileUpdateRequest {

    @Size(max = 150)
    private String businessName;

    @Size(max = 100)
    private String ownerName;

    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Indian mobile number")
    private String mobile;

    @Size(max = 500)
    private String businessAddress;

    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String state;

    @Pattern(regexp = "^\\d{6}$", message = "Pincode must be 6 digits")
    private String pincode;

    @Size(max = 20)
    private String gst;

    @Size(max = 15)
    private String pan;

    @Size(max = 120)
    private String bankAccountHolder;

    @Size(max = 30)
    private String bankAccountNumber;

    @Size(max = 15)
    private String bankIfsc;

    @Size(max = 120)
    private String bankName;

    @Size(max = 100)
    private String upiId;
}
