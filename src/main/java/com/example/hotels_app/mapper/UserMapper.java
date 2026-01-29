package com.example.hotels_app.mapper;

import com.example.hotels_app.entity.User;
import com.example.hotels_app.model.request.UpsertUserRequest;
import com.example.hotels_app.model.response.UserResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toEntity(UpsertUserRequest request);

    UpsertUserRequest toRequest(User user);

    UserResponse toResponse(User user);
}
