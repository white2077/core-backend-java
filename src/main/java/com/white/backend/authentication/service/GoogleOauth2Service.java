package com.white.backend.authentication.service;

import com.white.backend.authentication.dto.request.GoogleTokenRequestDto;
import com.white.backend.authentication.dto.response.GoogleTokenResponseDto;
import com.white.backend.authentication.dto.response.GoogleUserInfoDto;
import com.white.backend.authentication.dto.response.TokenResponseDto;
import com.white.backend.authentication.entity.User;
import com.white.backend.authentication.error.AuthError;
import com.white.backend.authentication.repository.UserRepository;
import com.white.backend.shared.enums.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

/**
 * The GoogleOauth2Service class manages Google OAuth2 authentication, including:
 * <ul>
 *   <li>Exchanging an authorization code for Google access and refresh tokens.</li>
 *   <li>Retrieving user profile information from Google.</li>
 *   <li>Generating internal JWT access and refresh tokens for the authenticated user.</li>
 * </ul>
 * <p>
 * This service integrates with Google’s OAuth2 API using the {@link RestTemplate} to facilitate the process.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleOauth2Service {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    @Value("${spring.security.oauth2.client.provider.google.token-uri}")
    private String tokenEndpoint;

    @Value("${spring.security.oauth2.client.provider.google.user-info-uri}")
    private String userInfoEndpoint;

    private final JwtService jwtService;

    private final UserRepository userRepository;

    /**
     * Generates an internal JWT access and refresh token by first exchanging the authorization code
     * with Google for a Google access token, then retrieving user information from Google using that access token.
     * <p>
     * If successful, this method save user in database and returns a {@link TokenResponseDto} with an access token valid for 1 day
     * and a refresh token valid for 30 days.
     *
     * @param code the Google authorization code obtained after the user grants permission in the OAuth2 login flow.
     * @return TokenResponseDto containing internal JWT access and refresh tokens.
     * @throws com.white.backend.shared.exception.HttpResponseException if token exchange or user information retrieval fails.
     */
    public TokenResponseDto createTokenFromGoogleUser(String code) {

        try {

            log.info("{}, Creating token from Google code: {}", this.getClass().getSimpleName(), code);

            RestTemplate restTemplate = new RestTemplate();

            // Prepare request to exchange authorization code for Google tokens
            GoogleTokenRequestDto googleTokenRequest = GoogleTokenRequestDto.builder()
                    .code(code)
                    .client_id(clientId)
                    .client_secret(clientSecret)
                    .redirect_uri(redirectUri)
                    .grant_type("authorization_code")
                    .build();

            HttpEntity<GoogleTokenRequestDto> requestEntity = new HttpEntity<>(googleTokenRequest, new HttpHeaders());

            // Send request to Google's token endpoint
            ResponseEntity<GoogleTokenResponseDto> googleTokenResponse = restTemplate.exchange(
                    tokenEndpoint, HttpMethod.POST, requestEntity, GoogleTokenResponseDto.class);

            // Check if Google responded successfully and access token is present
            if (googleTokenResponse.getStatusCode().is2xxSuccessful()
                    && Objects.requireNonNull(googleTokenResponse.getBody()).access_token() != null) {

                // Retrieve user information using Google access token
                HttpHeaders userInfoHeaders = new HttpHeaders();

                userInfoHeaders.add("Authorization", "Bearer " + googleTokenResponse.getBody().access_token());

                HttpEntity<Void> userInfoRequest = new HttpEntity<>(userInfoHeaders);

                ResponseEntity<GoogleUserInfoDto> userResponse = restTemplate.exchange(
                        userInfoEndpoint, HttpMethod.GET, userInfoRequest, GoogleUserInfoDto.class);

                if (userResponse.getStatusCode().is2xxSuccessful()) {
                    GoogleUserInfoDto userBody = Objects.requireNonNull(userResponse.getBody());

                    // Create a new user entity from Google user information
                    User user = createUser(userBody);

                    userRepository.findByUsername(user.getUsername())
                            .ifPresentOrElse(
                                    existingUser -> user.setId(existingUser.getId()),
                                    () -> userRepository.save(user));

                    // Generate JWT tokens for the authenticated user
                    String accessToken = jwtService.generateToken(
                            user, 1, JwtService.TokenType.ACCESS_TOKEN);

                    String refreshToken = jwtService.generateToken(
                            user, 30, JwtService.TokenType.REFRESH_TOKEN);

                    log.info("{}, Token created for Google user: {}", this.getClass().getSimpleName(), userBody.name());

                    return TokenResponseDto.builder()
                            .accessToken(accessToken)
                            .refreshToken(refreshToken)
                            .build();

                } else {

                    throw AuthError.UNAUTHORIZED.exception();

                }
            } else {

                throw AuthError.UNAUTHORIZED.exception();

            }
        } catch (Exception e) {

            log.error("{}, Error creating token: {}", this.getClass().getSimpleName(), e.getMessage());

            throw AuthError.UNAUTHORIZED.exception();

        }

    }

    /**
     * Creates a {@link User} entity from Google user information.
     *
     * @param userInfo the {@link GoogleUserInfoDto} containing user details retrieved from Google.
     * @return User entity populated with Google user information.
     */
    private User createUser(GoogleUserInfoDto userInfo) {

        return User.builder()
                .username(userInfo.email())
                .role(Role.USER)
                .name(userInfo.name())
                .avatar(userInfo.picture())
                .email(userInfo.email())
                .build();

    }
}
