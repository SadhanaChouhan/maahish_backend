package com.maahish.service.impl;

import com.maahish.config.JwtProperties;
import com.maahish.dto.request.*;
import com.maahish.dto.response.AuthResponse;
import com.maahish.entity.Cart;
import com.maahish.entity.PendingRegistration;
import com.maahish.entity.RefreshToken;
import com.maahish.entity.User;
import com.maahish.enums.OtpPurpose;
import com.maahish.enums.UserRole;
import com.maahish.enums.UserStatus;
import com.maahish.entity.Seller;
import com.maahish.enums.NotificationReferenceType;
import com.maahish.enums.NotificationType;
import com.maahish.exception.BadRequestException;
import com.maahish.exception.ResourceNotFoundException;
import com.maahish.exception.UnauthorizedException;
import com.maahish.mail.MailService;
import com.maahish.repository.CartRepository;
import com.maahish.repository.PendingRegistrationRepository;
import com.maahish.repository.RefreshTokenRepository;
import com.maahish.repository.SellerRepository;
import com.maahish.repository.UserRepository;
import com.maahish.security.JwtTokenProvider;
import com.maahish.security.UserPrincipal;
import com.maahish.service.AuthService;
import com.maahish.service.NotificationService;
import com.maahish.service.OtpService;
import com.maahish.service.SellerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final NotificationService notificationService;

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        String email = request.getEmail().toLowerCase();
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
            notificationService.notifyAdmins(
                    NotificationType.NEW_USER_REGISTERED,
                    "New user registered",
                    user.getName() + " (" + email + ") has joined Maahish.",
                    NotificationReferenceType.USER,
                    user.getId()
            );
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
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
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
