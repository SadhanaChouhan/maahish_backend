package com.maahish.auth.service;

import com.maahish.cart.repository.CartRepository;
import com.maahish.config.JwtProperties;
import com.maahish.common.security.JwtTokenProvider;
import com.maahish.infrastructure.mail.service.MailService;
import com.maahish.notification.service.NotificationService;
import com.maahish.auth.enums.OtpPurpose;
import com.maahish.auth.entity.PendingRegistration;
import com.maahish.auth.repository.PendingRegistrationRepository;
import com.maahish.auth.util.RateLimitService;
import com.maahish.config.AuthRateLimitProperties;
import com.maahish.auth.repository.RefreshTokenRepository;
import com.maahish.auth.dto.request.RegisterRequest;
import com.maahish.seller.repository.SellerRepository;
import com.maahish.seller.service.SellerService;
import com.maahish.user.entity.User;
import com.maahish.user.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplRegisterTest {

    @Mock private UserRepository userRepository;
    @Mock private PendingRegistrationRepository pendingRegistrationRepository;
    @Mock private CartRepository cartRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private JwtProperties jwtProperties;
    @Mock private OtpService otpService;
    @Mock private MailService mailService;
    @Mock private SellerRepository sellerRepository;
    @Mock private SellerService sellerService;
    @Mock private NotificationService notificationService;
    @Mock private RateLimitService rateLimitService;
    @Mock private AuthRateLimitProperties authRateLimitProperties;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void register_hashesPasswordBeforeSavingPendingRegistration() {
        when(authRateLimitProperties.getMaxRegisterAttemptsPerHour()).thenReturn(5);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(pendingRegistrationRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("plain-password")).thenReturn("$2a$hashed");
        when(pendingRegistrationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RegisterRequest request = new RegisterRequest();
        request.setName("Test User");
        request.setEmail("user@test.com");
        request.setPassword("plain-password");

        authService.register(request);

        ArgumentCaptor<PendingRegistration> captor = ArgumentCaptor.forClass(PendingRegistration.class);
        verify(pendingRegistrationRepository).save(captor.capture());
        assertEquals("$2a$hashed", captor.getValue().getPassword());
        verify(passwordEncoder).encode("plain-password");
        verify(otpService).generateAndSendOtp("user@test.com", com.maahish.auth.enums.OtpPurpose.REGISTRATION);
    }
}
