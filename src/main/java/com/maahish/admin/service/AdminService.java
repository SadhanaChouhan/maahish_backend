package com.maahish.admin.service;

import com.maahish.admin.dto.response.AdminDashboardResponse;
import com.maahish.common.dto.response.PageResponse;
import com.maahish.common.dto.response.UserResponse;
import com.maahish.common.enums.UserStatus;


public interface AdminService {

    AdminDashboardResponse getDashboard();

    PageResponse<UserResponse> getUsers(UserStatus status, int page, int size);

    UserResponse getUserById(Long userId);

    UserResponse updateUserStatus(Long userId, UserStatus status);
}
