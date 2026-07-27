package com.maahish.auth.service;

import com.maahish.auth.dto.response.AuthResponse;
import com.maahish.auth.dto.request.ForgotPasswordRequest;
import com.maahish.auth.dto.request.LoginRequest;
import com.maahish.auth.dto.request.RegisterRequest;
import com.maahish.auth.dto.request.ResetPasswordRequest;
import com.maahish.auth.dto.request.VerifyOtpRequest;


public interface AuthService {

    void register(RegisterRequest request);

    AuthResponse verifyOtpAndActivate(VerifyOtpRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(String refreshToken);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    void logout(Long userId);
}
