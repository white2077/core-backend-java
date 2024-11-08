package com.white.backend.authentication.service;

import com.white.backend.authentication.dto.request.UserLoginRequestDto;
import com.white.backend.authentication.dto.response.TokenResponseDto;
import com.white.backend.authentication.error.AuthError;
import com.white.backend.authentication.repository.UserRepository;
import com.white.backend.shared.exception.HttpResponseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * AuthService handles user authentication processes, including verifying login credentials,
 * generating access and refresh tokens, and refreshing access tokens when necessary.
 * <p>
 * This service relies on the {@link UserRepository} to retrieve user data, the {@link PasswordEncoder}
 * to verify passwords, and the {@link JwtService} to manage token generation and validation.
 * <p>
 * Key operations include:
 * <ul>
 *   <li>Authenticating a user with username and password, returning an access and refresh token.</li>
 *   <li>Refreshing access tokens based on a valid refresh token.</li>
 * </ul>
 * Each method logs important steps and outcomes to assist with debugging and monitoring.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    /**
     * Authenticates a user based on provided login details and generates access and refresh tokens upon successful verification.
     * <p>
     * This method checks the username in the database, verifies the password, and then uses the {@link JwtService} to create
     * a new access token (valid for 1 day) and a refresh token (valid for 30 days). If authentication fails, a custom
     * {@link HttpResponseException} with error code UNAUTHORIZED is thrown.
     *
     * @param userLoginRequestDto DTO containing the user's login credentials, specifically username and password.
     * @return {@link TokenResponseDto} object with an access token (valid for 1 day) and a refresh token (valid for 30 days).
     * @throws HttpResponseException if authentication fails due to invalid username or incorrect password.
     */
    public TokenResponseDto authenticated(UserLoginRequestDto userLoginRequestDto) {

        try {

            log.info("{}, Authenticating user: {}", this.getClass().getSimpleName(), userLoginRequestDto.username());

            // Retrieve user by username, or throw if not found
            var user = userRepository.findByUsername(userLoginRequestDto.username())
                    .orElseThrow(AuthError.INVALID_USERNAME_OR_PASSWORD::exception);

            // Verify password matches stored hash
            boolean authenticated = passwordEncoder.matches(userLoginRequestDto.password(), user.getPassword());

            if (!authenticated) {
                throw AuthError.INVALID_USERNAME_OR_PASSWORD.exception();
            }

            // Generate tokens upon successful authentication
            String token = jwtService.generateToken(user, 1, JwtService.TokenType.ACCESS_TOKEN);

            String refreshToken = jwtService.generateToken(user, 30, JwtService.TokenType.REFRESH_TOKEN);

            log.info("{}, User authenticated: {}", this.getClass().getSimpleName(), userLoginRequestDto.username());

            return TokenResponseDto.builder()
                    .accessToken(token)
                    .refreshToken(refreshToken)
                    .build();

        } catch (Exception e) {

            log.error("{}, Error authenticating user: {}", this.getClass().getSimpleName(), userLoginRequestDto.username());

            throw AuthError.UNAUTHORIZED.exception();
        }

    }

    /**
     * Refreshes an access token using a provided refresh token.
     * <p>
     * This method verifies the validity of the refresh token. Upon successful verification, it calls
     * {@link JwtService#refreshToken(String)} to generate a new access token. If the refresh token is invalid,
     * expired, or otherwise unusable, an {@link HttpResponseException} with error code UNAUTHORIZED is thrown.
     *
     * @param refreshToken A valid refresh token to be used to obtain a new access token.
     * @return {@link TokenResponseDto} object containing the newly generated access token.
     * @throws HttpResponseException if the refresh token is invalid or expired.
     */
    public TokenResponseDto refreshToken(String refreshToken) {

        try {

            log.info("{}, Refreshing token: {}", this.getClass().getSimpleName(), refreshToken);

            // Refresh token generation through JwtService
            TokenResponseDto tokenResponseDto = jwtService.refreshToken(refreshToken);

            log.info("{}, Token refreshed: {}", this.getClass().getSimpleName(), refreshToken);

            return tokenResponseDto;

        } catch (Exception e) {

            log.error("{}, Error refreshing token: {}", this.getClass().getSimpleName(), refreshToken);

            throw AuthError.UNAUTHORIZED.exception();

        }

    }

}
