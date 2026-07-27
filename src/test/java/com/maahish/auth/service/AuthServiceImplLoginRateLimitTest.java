package com.maahish.auth.service;

import com.maahish.config.AuthRateLimitProperties;
import com.maahish.common.exception.BadRequestException;
import com.maahish.cart.repository.CartRepository;
import com.maahish.auth.dto.request.ForgotPasswordRequest;
import com.maahish.config.JwtProperties;
import com.maahish.common.security.JwtTokenProvider;
import com.maahish.auth.dto.request.LoginRequest;
import com.maahish.auth.dto.request.RegisterRequest;
import com.maahish.infrastructure.mail.service.MailService;
import com.maahish.notification.service.NotificationService;
import com.maahish.auth.repository.PendingRegistrationRepository;
import com.maahish.auth.util.RateLimitService;
import com.maahish.auth.repository.RefreshTokenRepository;
import com.maahish.seller.repository.SellerRepository;
import com.maahish.seller.service.SellerService;
import com.maahish.user.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplLoginRateLimitTest {

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
    void login_whenRateLimitExceeded_throwsBeforeLookup() {
        when(authRateLimitProperties.getMaxLoginAttemptsPerHour()).thenReturn(10);
        doThrow(new BadRequestException("Too many requests. Please try again later."))
                .when(rateLimitService)
                .assertAllowed(eq("auth-login:user@test.com"), eq(10), any());

        LoginRequest request = new LoginRequest();
        request.setEmail("user@test.com");
        request.setPassword("password");

        assertThrows(BadRequestException.class, () -> authService.login(request));
    }

    @Test
    void forgotPassword_whenRateLimitExceeded_throwsBeforeEmailLookup() {
        when(authRateLimitProperties.getMaxForgotPasswordAttemptsPerHour()).thenReturn(5);
        doThrow(new BadRequestException("Too many requests. Please try again later."))
                .when(rateLimitService)
                .assertAllowed(eq("auth-forgot-password:user@test.com"), eq(5), any());

        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("user@test.com");

        assertThrows(BadRequestException.class, () -> authService.forgotPassword(request));
        verify(rateLimitService).assertAllowed(eq("auth-forgot-password:user@test.com"), eq(5), any());
    }

    @Test
    void register_whenRateLimitExceeded_throwsBeforeEmailLookup() {
        when(authRateLimitProperties.getMaxRegisterAttemptsPerHour()).thenReturn(5);
        doThrow(new BadRequestException("Too many requests. Please try again later."))
                .when(rateLimitService)
                .assertAllowed(eq("auth-register:user@test.com"), eq(5), any());

        RegisterRequest request = new RegisterRequest();
        request.setEmail("user@test.com");
        request.setPassword("password");
        request.setName("Test User");

        assertThrows(BadRequestException.class, () -> authService.register(request));
        verify(rateLimitService).assertAllowed(eq("auth-register:user@test.com"), eq(5), any());
    }
}
