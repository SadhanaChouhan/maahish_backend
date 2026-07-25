package com.maahish.service;

import com.maahish.enums.OtpPurpose;

public interface OtpService {

    void generateAndSendOtp(String email, OtpPurpose purpose);

    void verifyOtp(String email, String otp, OtpPurpose purpose);
}
