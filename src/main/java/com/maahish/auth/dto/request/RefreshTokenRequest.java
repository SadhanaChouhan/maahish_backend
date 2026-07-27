package com.maahish.auth.dto.request;

import lombok.Data;

@Data
public class RefreshTokenRequest {

    /** Optional when refresh token is sent via httpOnly cookie. */
    private String refreshToken;
}
