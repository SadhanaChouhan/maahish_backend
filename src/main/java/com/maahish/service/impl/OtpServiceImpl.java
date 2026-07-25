package com.maahish.service.impl;

import com.maahish.config.OtpProperties;
import com.maahish.entity.OtpVerification;
import com.maahish.enums.OtpPurpose;
import com.maahish.exception.BadRequestException;
import com.maahish.mail.MailService;
import com.maahish.repository.OtpVerificationRepository;
import com.maahish.service.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private final OtpVerificationRepository otpRepository;
    private final OtpProperties otpProperties;
    private final MailService mailService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public void generateAndSendOtp(String email, OtpPurpose purpose) {
        String otp = generateOtp();
        OtpVerification verification = OtpVerification.builder()
                .email(email.toLowerCase())
                .otp(otp)
                .purpose(purpose)
                .expiresAt(LocalDateTime.now().plusMinutes(otpProperties.getExpirationMinutes()))
                .verified(false)
                .createdAt(LocalDateTime.now())
                .build();
        otpRepository.save(verification);
        mailService.sendOtpEmail(email, otp, purpose.name());
        log.info("OTP generated for {} purpose {}", email, purpose);
    }

    @Override
    @Transactional
    public void verifyOtp(String email, String otp, OtpPurpose purpose) {
        OtpVerification verification = otpRepository
                .findTopByEmailAndPurposeAndVerifiedFalseOrderByCreatedAtDesc(email.toLowerCase(), purpose)
                .orElseThrow(() -> new BadRequestException("OTP not found or already used"));

        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("OTP has expired");
        }
        if (!verification.getOtp().equals(otp)) {
            throw new BadRequestException("Invalid OTP");
        }
        verification.setVerified(true);
        otpRepository.save(verification);
    }

    private String generateOtp() {
        int bound = (int) Math.pow(10, otpProperties.getLength());
        int otp = secureRandom.nextInt(bound / 10, bound);
        return String.valueOf(otp);
    }
}
