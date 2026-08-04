package com.maahish.auth.controller;

import com.maahish.common.constants.AppConstants;
import com.maahish.common.dto.response.ApiResponse;
import com.maahish.common.exception.UnauthorizedException;
import com.maahish.common.security.AuthCookieService;
import com.maahish.common.security.SecurityUtil;
import com.maahish.auth.dto.request.ForgotPasswordRequest;
import com.maahish.auth.dto.request.LoginRequest;
import com.maahish.auth.dto.request.RefreshTokenRequest;
import com.maahish.auth.dto.request.RegisterRequest;
import com.maahish.auth.dto.request.ResetPasswordRequest;
import com.maahish.auth.dto.request.VerifyOtpRequest;
import com.maahish.auth.dto.response.AuthResponse;
import com.maahish.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register, login, OTP verification, password reset")
public class AuthController {

    private final AuthService authService;
    private final AuthCookieService authCookieService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful. Please verify OTP sent to your email."));
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify email OTP")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request,
            HttpServletResponse response) {
        AuthResponse authResponse = authService.verifyOtpAndActivate(request);
        authCookieService.writeAuthCookies(response, authResponse);
        return ResponseEntity.ok(ApiResponse.success(
                "OTP verified successfully",
                authCookieService.toClientResponse(authResponse)));
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email and password")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {
        AuthResponse authResponse = authService.login(request);
        authCookieService.writeAuthCookies(response, authResponse);
        return ResponseEntity.ok(ApiResponse.success(
                "Login successful",
                authCookieService.toClientResponse(authResponse)));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh access token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @RequestBody(required = false) RefreshTokenRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response) {
        String refreshToken = resolveRefreshToken(httpRequest, request);
        AuthResponse authResponse = authService.refreshToken(refreshToken);
        authCookieService.writeAuthCookies(response, authResponse);
        return ResponseEntity.ok(ApiResponse.success(authCookieService.toClientResponse(authResponse)));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset OTP")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success("OTP sent to your email"));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password with OTP")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password reset successful"));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout and revoke refresh tokens")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletResponse response) {
        authService.logout(SecurityUtil.getCurrentUserId());
        authCookieService.clearAuthCookies(response);
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }

    private String resolveRefreshToken(HttpServletRequest httpRequest, RefreshTokenRequest request) {
        if (httpRequest.getCookies() != null) {
            for (Cookie cookie : httpRequest.getCookies()) {
                if (AppConstants.REFRESH_TOKEN_COOKIE.equals(cookie.getName())
                        && StringUtils.hasText(cookie.getValue())) {
                    return cookie.getValue();
                }
            }
        }
        if (request != null && StringUtils.hasText(request.getRefreshToken())) {
            return request.getRefreshToken();
        }
        throw new UnauthorizedException("Refresh token required");
    }
}
