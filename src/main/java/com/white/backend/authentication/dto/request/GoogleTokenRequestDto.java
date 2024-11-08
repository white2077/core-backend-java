package com.white.backend.authentication.dto.request;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record GoogleTokenRequestDto(

        String code,

        String client_id,

        String client_secret,

        String redirect_uri,

        String grant_type

) implements Serializable {
}
