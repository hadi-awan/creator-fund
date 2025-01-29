package com.creatorfund.mapper;

import com.creatorfund.dto.request.CreateUserRequest;
import com.creatorfund.dto.response.UserProfileResponse;
import com.creatorfund.dto.response.UserSummaryResponse;
import com.creatorfund.model.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserSummaryResponse toSummaryResponse(User user);
    UserProfileResponse toProfileResponse(User user);

    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "role", constant = "BACKER")  // Changed from "USER" to "BACKER"
    @Mapping(target = "status", constant = "ACTIVE")
    User toEntity(CreateUserRequest request);
}
