package com.maahish.service;

import com.maahish.dto.response.AdminDashboardResponse;
import com.maahish.dto.response.PageResponse;
import com.maahish.dto.response.UserResponse;
import com.maahish.enums.UserStatus;

public interface AdminService {

    AdminDashboardResponse getDashboard();

    PageResponse<UserResponse> getUsers(UserStatus status, int page, int size);

    UserResponse getUserById(Long userId);

    UserResponse updateUserStatus(Long userId, UserStatus status);
}
