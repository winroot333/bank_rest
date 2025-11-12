package com.example.bankcards.service.mapper;

import com.example.bankcards.dto.request.SignUpRequest;
import com.example.bankcards.dto.response.UserResponse;
import com.example.bankcards.entity.User;
import org.mapstruct.*;
import org.springframework.security.crypto.password.PasswordEncoder;


/**
 * Мэппер для преобразования Сущностей пользователей в ДТО
 */
@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toResponse(User user);

    @Mapping(target = "role", constant = "ROLE_USER")
    @Mapping(target = "password", qualifiedByName = "encode")
    User toEntityWithEncodedPassword(SignUpRequest signUpRequest, @Context PasswordEncoder passwordEncoder);

    @Named("encode")
    default String encodePassword(String rawPassword, @Context PasswordEncoder passwordEncoder) {
        return passwordEncoder.encode(rawPassword);
    }
}
