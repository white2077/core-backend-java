package com.white.backend.auth.dto.request;

import com.white.backend.auth.entity.User;

import java.io.Serializable;

/**
 * DTO for {@link User}
 */
public record UserLoginRequestDto(String username, String password) implements Serializable {
}