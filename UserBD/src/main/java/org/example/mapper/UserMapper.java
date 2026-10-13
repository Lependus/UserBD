package org.example.mapper;

import org.example.User;
import org.example.dto.UserRequest;
import org.example.dto.UserResponse;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(UserRequest userRequest){
        return new User(userRequest.name(), userRequest.email(), userRequest.age());
    }

    public UserResponse toResponse(User user){
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getAge(), user.getCreatedAt());
    }
}
