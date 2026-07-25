package com.maahish.mapper;

import com.maahish.dto.response.UserResponse;
import com.maahish.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toUserResponse(User user);
}
