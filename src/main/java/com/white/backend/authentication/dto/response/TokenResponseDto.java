package com.white.backend.authentication.dto.response;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record TokenResponseDto(

        String accessToken,

        String refreshToken

) implements Serializable {
}
