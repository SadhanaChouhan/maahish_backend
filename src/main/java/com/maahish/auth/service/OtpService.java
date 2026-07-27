package com.maahish.auth.service;

import com.maahish.auth.enums.OtpPurpose;


public interface OtpService {

    void generateAndSendOtp(String email, OtpPurpose purpose);

    void verifyOtp(String email, String otp, OtpPurpose purpose);
}
