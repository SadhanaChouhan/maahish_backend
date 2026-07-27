package com.maahish.user.mapper;

import com.maahish.user.entity.User;
import com.maahish.common.dto.response.UserResponse;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toUserResponse(User user);
}
