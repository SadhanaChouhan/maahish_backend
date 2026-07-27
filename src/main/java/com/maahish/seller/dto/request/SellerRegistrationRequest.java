package com.maahish.seller.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SellerRegistrationRequest {

    @NotBlank(message = "Business name is required")
    @Size(max = 150)
    private String businessName;

    @NotBlank(message = "Owner name is required")
    @Size(max = 100)
    private String ownerName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Mobile is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Indian mobile number")
    private String mobile;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "Business address is required")
    @Size(max = 500)
    private String businessAddress;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "Pincode is required")
    @Pattern(regexp = "^\\d{6}$", message = "Pincode must be 6 digits")
    private String pincode;

    @Size(max = 20)
    private String gst;

    @Size(max = 15)
    private String pan;

    @NotBlank(message = "Bank account holder name is required")
    private String bankAccountHolder;

    @NotBlank(message = "Bank account number is required")
    private String bankAccountNumber;

    @NotBlank(message = "Bank IFSC is required")
    private String bankIfsc;

    @NotBlank(message = "Bank name is required")
    private String bankName;

    @Size(max = 100)
    private String upiId;
}
