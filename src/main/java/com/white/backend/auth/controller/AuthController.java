package com.white.backend.auth.controller;

import com.white.backend.auth.dto.request.UserLoginRequestDto;
import com.white.backend.auth.dto.response.TokenResponseDto;
import com.white.backend.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication - API", description = "All operations related to authentication")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(
            summary = "User Login Endpoint",
            description = "Verifies the user's credentials (username and password). If valid, returns an " +
                    "access token and a refresh token for the authenticated session. The access token " +
                    "is used to authorize further requests, while the refresh token is used to obtain a new " +
                    "access token when the current one expires.")
    public TokenResponseDto login(@RequestBody UserLoginRequestDto userLoginRequestDto) {
        return authService.authenticated(userLoginRequestDto);
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "Refresh Access Token",
            description = "Generates a new access token using the provided refresh token if it is valid. " +
                    "Returns both the new access token and the original refresh token. This endpoint allows " +
                    "continued access without requiring re-authentication when the access token expires.")
    public TokenResponseDto refreshToken(@RequestBody String refreshToken) {
        return authService.refreshToken(refreshToken);
    }

}
