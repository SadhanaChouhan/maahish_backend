package com.maahish.dto.response;

import com.maahish.enums.UserRole;
import com.maahish.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private String mobile;
    private UserRole role;
    private UserStatus status;
    private LocalDateTime createdAt;
}
