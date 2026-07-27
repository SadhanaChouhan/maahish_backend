package com.maahish.auth.service;

import com.maahish.config.AuthRateLimitProperties;
import com.maahish.auth.dto.response.AuthResponse;
import com.maahish.common.exception.BadRequestException;
import com.maahish.cart.entity.Cart;
import com.maahish.cart.repository.CartRepository;
import com.maahish.auth.dto.request.ForgotPasswordRequest;
import com.maahish.config.JwtProperties;
import com.maahish.common.security.JwtTokenProvider;
import com.maahish.auth.dto.request.LoginRequest;
import com.maahish.infrastructure.mail.service.MailService;
import com.maahish.auth.enums.OtpPurpose;
import com.maahish.auth.entity.PendingRegistration;
import com.maahish.auth.repository.PendingRegistrationRepository;
import com.maahish.auth.util.RateLimitService;
import com.maahish.auth.entity.RefreshToken;
import com.maahish.auth.repository.RefreshTokenRepository;
import com.maahish.auth.dto.request.RegisterRequest;
import com.maahish.auth.dto.request.ResetPasswordRequest;
import com.maahish.common.exception.ResourceNotFoundException;
import com.maahish.seller.entity.Seller;
import com.maahish.seller.repository.SellerRepository;
import com.maahish.seller.service.SellerService;
import com.maahish.common.exception.UnauthorizedException;
import com.maahish.user.entity.User;
import com.maahish.common.security.UserPrincipal;
import com.maahish.user.repository.UserRepository;
import com.maahish.common.enums.UserRole;
import com.maahish.common.enums.UserStatus;
import com.maahish.auth.dto.request.VerifyOtpRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PendingRegistrationRepository pendingRegistrationRepository;
    private final CartRepository cartRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final OtpService otpService;
    private final MailService mailService;
    private final SellerRepository sellerRepository;
    private final SellerService sellerService;
    private final RateLimitService rateLimitService;
    private final AuthRateLimitProperties authRateLimitProperties;

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        String email = request.getEmail().toLowerCase();
        rateLimitService.assertAllowed(
                "auth-register:" + email,
                authRateLimitProperties.getMaxRegisterAttemptsPerHour(),
                Duration.ofHours(1));

        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email already registered");
        }
        if (request.getMobile() != null && userRepository.existsByMobile(request.getMobile())) {
            throw new BadRequestException("Mobile number already registered");
        }
        if (request.getMobile() != null) {
            Optional<PendingRegistration> mobilePending = pendingRegistrationRepository.findByMobile(request.getMobile());
            if (mobilePending.isPresent() && !mobilePending.get().getEmail().equals(email)) {
                throw new BadRequestException("Mobile number already registered");
            }
        }

        LocalDateTime now = LocalDateTime.now();
        PendingRegistration pending = pendingRegistrationRepository.findByEmail(email)
                .orElse(PendingRegistration.builder().email(email).createdAt(now).build());
        pending.setName(request.getName());
        pending.setMobile(request.getMobile());
        pending.setPassword(passwordEncoder.encode(request.getPassword()));
        pending.setExpiresAt(now.plusHours(24));
        pendingRegistrationRepository.save(pending);

        otpService.generateAndSendOtp(email, OtpPurpose.REGISTRATION);
        log.info("Registration pending OTP verification: {}", email);
    }

    @Override
    @Transactional
    public AuthResponse verifyOtpAndActivate(VerifyOtpRequest request) {
        String email = request.getEmail().toLowerCase();
        otpService.verifyOtp(email, request.getOtp(), request.getPurpose());

        if (request.getPurpose() == OtpPurpose.REGISTRATION) {
            PendingRegistration pending = pendingRegistrationRepository.findByEmail(email)
                    .orElseThrow(() -> new BadRequestException("Registration session expired. Please register again."));
            if (pending.getExpiresAt().isBefore(LocalDateTime.now())) {
                pendingRegistrationRepository.delete(pending);
                throw new BadRequestException("Registration session expired. Please register again.");
            }
            if (userRepository.existsByEmail(email)) {
                pendingRegistrationRepository.deleteByEmail(email);
                throw new BadRequestException("Email already registered");
            }

            User user = User.builder()
                    .name(pending.getName())
                    .email(email)
                    .mobile(pending.getMobile())
                    .password(pending.getPassword())
                    .role(UserRole.ROLE_USER)
                    .status(UserStatus.ACTIVE)
                    .build();
            userRepository.save(user);

            Cart cart = Cart.builder().user(user).build();
            cartRepository.save(cart);

            pendingRegistrationRepository.delete(pending);
            mailService.sendWelcomeEmail(email, user.getName());
            log.info("User registered after OTP verification: {}", email);
            return buildAuthResponse(user);
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return buildAuthResponse(user);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().toLowerCase();
        rateLimitService.assertAllowed(
                "auth-login:" + email,
                authRateLimitProperties.getMaxLoginAttemptsPerHour(),
                Duration.ofHours(1));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BadRequestException("Account is not active");
        }

        if (user.getRole() == UserRole.ROLE_SELLER) {
            Seller seller = sellerRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new BadRequestException("Seller profile not found"));
            sellerService.assertSellerCanLogin(seller);
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword())
        );
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return buildAuthResponse(userRepository.findById(principal.getId()).orElseThrow());
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (refreshToken.getRevoked() || refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException("Refresh token expired or revoked");
        }

        User user = userRepository.findById(refreshToken.getUser().getId())
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("Account is not active");
        }

        if (user.getRole() == UserRole.ROLE_SELLER) {
            Seller seller = sellerRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new UnauthorizedException("Seller profile not found"));
            sellerService.assertSellerCanLogin(seller);
        }

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        return buildAuthResponse(user);
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        String email = request.getEmail().toLowerCase();
        rateLimitService.assertAllowed(
                "auth-forgot-password:" + email,
                authRateLimitProperties.getMaxForgotPasswordAttemptsPerHour(),
                Duration.ofHours(1));

        if (!userRepository.existsByEmail(email)) {
            throw new BadRequestException("No account found with this email");
        }
        otpService.generateAndSendOtp(email, OtpPurpose.FORGOT_PASSWORD);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String email = request.getEmail().toLowerCase();
        otpService.verifyOtp(email, request.getOtp(), OtpPurpose.FORGOT_PASSWORD);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        refreshTokenRepository.deleteByUser(user);
        log.info("Password reset for {}", email);
    }

    @Override
    @Transactional
    public void logout(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        refreshTokenRepository.deleteByUser(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        UserPrincipal principal = UserPrincipal.from(user);
        String accessToken = jwtTokenProvider.generateAccessToken(principal);
        String refreshTokenValue = jwtTokenProvider.generateRefreshTokenValue();

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(refreshTokenValue)
                .expiresAt(LocalDateTime.now().plusSeconds(jwtProperties.getRefreshTokenExpirationMs() / 1000))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .tokenType("Bearer")
                .expiresIn(jwtProperties.getAccessTokenExpirationMs() / 1000)
                .user(AuthResponse.UserSummaryResponse.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .email(user.getEmail())
                        .mobile(user.getMobile())
                        .role(user.getRole())
                        .status(user.getStatus())
                        .build())
                .build();
    }
}
