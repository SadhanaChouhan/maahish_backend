package com.maahish.service;

import com.maahish.dto.request.*;
import com.maahish.dto.response.AuthResponse;

public interface AuthService {

    void register(RegisterRequest request);

    AuthResponse verifyOtpAndActivate(VerifyOtpRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    void logout(Long userId);
}
