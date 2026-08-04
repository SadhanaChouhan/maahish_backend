package com.maahish.user.controller;

import com.maahish.common.dto.response.ApiResponse;
import com.maahish.common.exception.ResourceNotFoundException;
import com.maahish.common.security.AuthCookieService;
import com.maahish.common.security.SecurityUtil;
import com.maahish.user.dto.request.DeleteAccountRequest;
import com.maahish.user.entity.User;
import com.maahish.user.mapper.UserMapper;
import com.maahish.user.repository.UserRepository;
import com.maahish.user.service.ProfileService;
import com.maahish.common.dto.response.UserResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/profile")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "User profile")
public class ProfileController {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final ProfileService profileService;
    private final AuthCookieService authCookieService;

    @GetMapping
    @Operation(summary = "Get current user profile")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile() {
        Long userId = SecurityUtil.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(ApiResponse.success(userMapper.toUserResponse(user)));
    }

    @DeleteMapping("/account")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Delete customer account (requires password confirmation)")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(
            @Valid @RequestBody DeleteAccountRequest request,
            HttpServletResponse response) {
        profileService.deleteAccount(SecurityUtil.getCurrentUserId(), request);
        authCookieService.clearAuthCookies(response);
        return ResponseEntity.ok(ApiResponse.success("Account deleted successfully"));
    }
}
