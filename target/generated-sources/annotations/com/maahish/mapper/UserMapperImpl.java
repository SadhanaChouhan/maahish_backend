package com.maahish.mapper;

import com.maahish.dto.response.UserResponse;
import com.maahish.entity.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-25T21:32:43+0530",
    comments = "version: 1.6.2, compiler: javac, environment: Java 21.0.2 (Oracle Corporation)"
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
