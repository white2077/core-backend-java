package com.white.backend.authentication.dto.request;

import com.white.backend.authentication.entity.User;

import java.io.Serializable;

/**
 * DTO for {@link User}
 */
public record UserLoginRequestDto(

        String username,

        String password

) implements Serializable {
}