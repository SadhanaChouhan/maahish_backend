package com.maahish.auth.service;

import com.maahish.common.exception.BadRequestException;
import com.maahish.infrastructure.mail.service.MailService;
import com.maahish.config.OtpProperties;
import com.maahish.auth.enums.OtpPurpose;
import com.maahish.auth.entity.OtpVerification;
import com.maahish.auth.repository.OtpVerificationRepository;

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
        String normalizedEmail = email.toLowerCase();
        LocalDateTime now = LocalDateTime.now();

        long sentLastHour = otpRepository.countByEmailAndPurposeAndCreatedAtAfter(
                normalizedEmail, purpose, now.minusHours(1));
        if (sentLastHour >= otpProperties.getMaxSendsPerHour()) {
            throw new BadRequestException("Too many OTP requests. Please try again later.");
        }

        otpRepository.findTopByEmailAndPurposeOrderByCreatedAtDesc(normalizedEmail, purpose)
                .ifPresent(latest -> {
                    LocalDateTime cooldownUntil = latest.getCreatedAt()
                            .plusSeconds(otpProperties.getResendCooldownSeconds());
                    if (cooldownUntil.isAfter(now)) {
                        throw new BadRequestException("Please wait before requesting another OTP.");
                    }
                });

        String otp = generateOtp();
        OtpVerification verification = OtpVerification.builder()
                .email(normalizedEmail)
                .otp(otp)
                .purpose(purpose)
                .expiresAt(now.plusMinutes(otpProperties.getExpirationMinutes()))
                .verified(false)
                .failedAttempts(0)
                .createdAt(now)
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

        int attempts = verification.getFailedAttempts() != null ? verification.getFailedAttempts() : 0;
        if (attempts >= otpProperties.getMaxVerifyAttempts()) {
            throw new BadRequestException("Too many invalid OTP attempts. Please request a new OTP.");
        }

        if (!verification.getOtp().equals(otp)) {
            verification.setFailedAttempts(attempts + 1);
            otpRepository.save(verification);
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
