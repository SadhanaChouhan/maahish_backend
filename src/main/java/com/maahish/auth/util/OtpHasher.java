package com.maahish.auth.util;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OtpHasher {

    private final PasswordEncoder passwordEncoder;

    public String hash(String otp) {
        return passwordEncoder.encode(otp);
    }

    public boolean matches(String rawOtp, String storedHash) {
        if (rawOtp == null || storedHash == null) {
            return false;
        }
        return passwordEncoder.matches(rawOtp, storedHash);
    }
}
