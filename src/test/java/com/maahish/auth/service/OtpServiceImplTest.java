package com.maahish.auth.service;

import com.maahish.common.exception.BadRequestException;
import com.maahish.infrastructure.mail.service.MailService;
import com.maahish.config.OtpProperties;
import com.maahish.auth.enums.OtpPurpose;
import com.maahish.auth.entity.OtpVerification;
import com.maahish.auth.repository.OtpVerificationRepository;
import com.maahish.auth.util.OtpHasher;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpServiceImplTest {

    @Mock private OtpVerificationRepository otpRepository;
    @Mock private MailService mailService;

    private OtpProperties otpProperties;
    private OtpHasher otpHasher;
    private OtpServiceImpl otpService;

    @BeforeEach
    void setUp() {
        otpProperties = new OtpProperties();
        otpProperties.setExpirationMinutes(10);
        otpProperties.setLength(6);
        otpProperties.setMaxVerifyAttempts(3);
        otpProperties.setResendCooldownSeconds(60);
        otpProperties.setMaxSendsPerHour(5);
        otpHasher = new OtpHasher(new BCryptPasswordEncoder());
        otpService = new OtpServiceImpl(otpRepository, otpProperties, mailService, otpHasher);
    }

    @Test
    void verifyOtp_invalidCode_incrementsFailedAttempts() {
        OtpVerification verification = OtpVerification.builder()
                .email("user@test.com")
                .otp(otpHasher.hash("123456"))
                .purpose(OtpPurpose.REGISTRATION)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .verified(false)
                .failedAttempts(0)
                .createdAt(LocalDateTime.now())
                .build();

        when(otpRepository.findTopByEmailAndPurposeAndVerifiedFalseOrderByCreatedAtDesc(
                "user@test.com", OtpPurpose.REGISTRATION)).thenReturn(Optional.of(verification));

        assertThrows(BadRequestException.class,
                () -> otpService.verifyOtp("user@test.com", "000000", OtpPurpose.REGISTRATION));

        assertEquals(1, verification.getFailedAttempts());
        verify(otpRepository).save(verification);
    }

    @Test
    void verifyOtp_tooManyFailedAttempts_rejects() {
        OtpVerification verification = OtpVerification.builder()
                .email("user@test.com")
                .otp(otpHasher.hash("123456"))
                .purpose(OtpPurpose.REGISTRATION)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .verified(false)
                .failedAttempts(3)
                .createdAt(LocalDateTime.now())
                .build();

        when(otpRepository.findTopByEmailAndPurposeAndVerifiedFalseOrderByCreatedAtDesc(
                "user@test.com", OtpPurpose.REGISTRATION)).thenReturn(Optional.of(verification));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> otpService.verifyOtp("user@test.com", "123456", OtpPurpose.REGISTRATION));
        assertTrue(ex.getMessage().contains("Too many invalid OTP attempts"));
    }

    @Test
    void generateOtp_respectsResendCooldown() {
        when(otpRepository.countByEmailAndPurposeAndCreatedAtAfter(anyString(), any(), any()))
                .thenReturn(0L);
        when(otpRepository.findTopByEmailAndPurposeOrderByCreatedAtDesc(anyString(), any()))
                .thenReturn(Optional.of(OtpVerification.builder()
                        .createdAt(LocalDateTime.now())
                        .build()));

        assertThrows(BadRequestException.class,
                () -> otpService.generateAndSendOtp("user@test.com", OtpPurpose.REGISTRATION));
    }

    @Test
    void generateOtp_storesHashedValue() {
        when(otpRepository.countByEmailAndPurposeAndCreatedAtAfter(anyString(), any(), any()))
                .thenReturn(0L);
        when(otpRepository.findTopByEmailAndPurposeOrderByCreatedAtDesc(anyString(), any()))
                .thenReturn(Optional.empty());
        when(otpRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        otpService.generateAndSendOtp("user@test.com", OtpPurpose.REGISTRATION);

        verify(otpRepository).save(argThat(saved ->
                saved.getOtp() != null
                        && !saved.getOtp().equals("123456")
                        && saved.getOtp().startsWith("$2")));
        verify(mailService).sendOtpEmail(eq("user@test.com"), anyString(), eq(OtpPurpose.REGISTRATION.name()));
    }
}
