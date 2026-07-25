package com.maahish.dto.response;

import com.maahish.enums.UserRole;
import com.maahish.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private UserSummaryResponse user;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserSummaryResponse {
        private Long id;
        private String name;
        private String email;
        private String mobile;
        private UserRole role;
        private UserStatus status;
    }
}
