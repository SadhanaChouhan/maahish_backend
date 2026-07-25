package com.maahish.controller;

import com.maahish.dto.response.ApiResponse;
import com.maahish.dto.response.UserResponse;
import com.maahish.entity.User;
import com.maahish.exception.ResourceNotFoundException;
import com.maahish.mapper.UserMapper;
import com.maahish.repository.UserRepository;
import com.maahish.security.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/profile")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "User profile")
public class ProfileController {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @GetMapping
    @Operation(summary = "Get current user profile")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile() {
        Long userId = SecurityUtil.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(ApiResponse.success(userMapper.toUserResponse(user)));
    }
}
