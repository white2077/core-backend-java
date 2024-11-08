package com.white.backend.authentication.controller;

import com.white.backend.authentication.dto.request.UserLoginRequestDto;
import com.white.backend.authentication.dto.response.TokenResponseDto;
import com.white.backend.authentication.service.AuthService;
import com.white.backend.authentication.service.GoogleOauth2Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication - API", description = "All operations related to authentication")
public class AuthController {

    private final AuthService authService;

    private final GoogleOauth2Service googleOauth2Service;

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

    @GetMapping("/login/oauth2/callback")
    @Operation(
            summary = "OAuth2 Callback Endpoint",
            description = "Receives the OAuth2 authorization code from the OAuth2 provider. " +
                    "This code is used to obtain the access token and refresh token from the OAuth2 provider.")
    public TokenResponseDto oauth2Callback(@RequestParam String code) {

        return googleOauth2Service.createTokenFromGoogleUser(code);

    }

}
