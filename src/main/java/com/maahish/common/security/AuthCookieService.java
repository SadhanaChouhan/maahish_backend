package com.maahish.common.security;

import com.maahish.common.constants.AppConstants;
import com.maahish.config.AuthCookieProperties;
import com.maahish.auth.dto.response.AuthResponse;
import com.maahish.config.JwtProperties;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthCookieService {

    private final AuthCookieProperties cookieProperties;
    private final JwtProperties jwtProperties;

    public void writeAuthCookies(HttpServletResponse response, AuthResponse authResponse) {
        int accessMaxAge = (int) (jwtProperties.getAccessTokenExpirationMs() / 1000);
        int refreshMaxAge = (int) (jwtProperties.getRefreshTokenExpirationMs() / 1000);

        response.addCookie(buildCookie(
                AppConstants.ACCESS_TOKEN_COOKIE,
                authResponse.getAccessToken(),
                AppConstants.ACCESS_TOKEN_COOKIE_PATH,
                accessMaxAge));
        response.addCookie(buildCookie(
                AppConstants.REFRESH_TOKEN_COOKIE,
                authResponse.getRefreshToken(),
                AppConstants.REFRESH_TOKEN_COOKIE_PATH,
                refreshMaxAge));
    }

    public void clearAuthCookies(HttpServletResponse response) {
        response.addCookie(buildCookie(
                AppConstants.ACCESS_TOKEN_COOKIE, "", AppConstants.ACCESS_TOKEN_COOKIE_PATH, 0));
        response.addCookie(buildCookie(
                AppConstants.REFRESH_TOKEN_COOKIE, "", AppConstants.REFRESH_TOKEN_COOKIE_PATH, 0));
    }

    public AuthResponse toClientResponse(AuthResponse authResponse) {
        return AuthResponse.builder()
                .tokenType(authResponse.getTokenType())
                .expiresIn(authResponse.getExpiresIn())
                .user(authResponse.getUser())
                .build();
    }

    private Cookie buildCookie(String name, String value, String path, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieProperties.isSecure());
        cookie.setPath(path);
        cookie.setMaxAge(maxAge);
        cookie.setAttribute("SameSite", cookieProperties.getSameSite());
        return cookie;
    }
}
