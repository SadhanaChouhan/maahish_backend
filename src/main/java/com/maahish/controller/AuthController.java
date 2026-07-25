package com.maahish.controller;

import com.maahish.dto.request.*;
import com.maahish.dto.response.ApiResponse;
import com.maahish.dto.response.AuthResponse;
import com.maahish.security.SecurityUtil;
import com.maahish.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register, login, OTP verification, password reset")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful. Please verify OTP sent to your email."));
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify email OTP")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        AuthResponse response = authService.verifyOtpAndActivate(request);
        return ResponseEntity.ok(ApiResponse.success("OTP verified successfully", response));
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email and password")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh access token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success(response));
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
    public ResponseEntity<ApiResponse<Void>> logout() {
        authService.logout(SecurityUtil.getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }
}
