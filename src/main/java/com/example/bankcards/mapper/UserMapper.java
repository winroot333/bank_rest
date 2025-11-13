package com.example.bankcards.mapper;

import com.example.bankcards.dto.request.SignUpRequest;
import com.example.bankcards.dto.response.PageResponse;
import com.example.bankcards.dto.response.UserResponse;
import com.example.bankcards.entity.User;
import org.mapstruct.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;


/**
 * Мэппер для преобразования Сущностей пользователей в ДТО
 */
@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    UserResponse toResponse(User user);

    @Mapping(target = "role", constant = "ROLE_USER")
    @Mapping(target = "password", qualifiedByName = "encode")
    User toEntityWithEncodedPassword(SignUpRequest signUpRequest, @Context PasswordEncoder passwordEncoder);

    default PageResponse<UserResponse> toPageResponse(Page<User> page) {
        List<UserResponse> content = page.getContent()
                .stream()
                .map(this::toResponse)
                .toList();

        return PageResponse.of(
                new PageImpl<>(content, page.getPageable(), page.getTotalElements())
        );
    }


    @Named("encode")
    default String encodePassword(String rawPassword, @Context PasswordEncoder passwordEncoder) {
        return passwordEncoder.encode(rawPassword);
    }
}
