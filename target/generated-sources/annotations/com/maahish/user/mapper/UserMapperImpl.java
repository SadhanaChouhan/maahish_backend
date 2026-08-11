package com.maahish.user.mapper;

import com.maahish.common.dto.response.UserResponse;
import com.maahish.user.entity.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-11T23:33:01+0530",
    comments = "version: 1.6.2, compiler: javac, environment: Java 21.0.12 (Oracle Corporation)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserResponse toUserResponse(User user) {
        if ( user == null ) {
            return null;
        }

        UserResponse.UserResponseBuilder userResponse = UserResponse.builder();

        userResponse.id( user.getId() );
        userResponse.name( user.getName() );
        userResponse.email( user.getEmail() );
        userResponse.mobile( user.getMobile() );
        userResponse.role( user.getRole() );
        userResponse.status( user.getStatus() );
        userResponse.createdAt( user.getCreatedAt() );

        return userResponse.build();
    }
}
