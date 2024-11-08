package com.white.backend.auth.service;

import com.white.backend.auth.dto.request.UserLoginRequestDto;
import com.white.backend.auth.dto.response.TokenResponseDto;
import com.white.backend.auth.error.AuthError;
import com.white.backend.auth.repository.UserRepository;
import com.white.backend.shared.exception.HttpResponseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * The AuthenticationService class is responsible for handling user authentication,
 * including login requests and generating access and refresh tokens upon successful authentication.
 * This class relies on UserRepository, PasswordEncoder, and JwtUtil to perform its operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;


    /**
     * Authenticates the user based on the provided login request data and returns access and refresh tokens.
     *
     * @param userLoginRequestDto a DTO containing the user's login information, including username and password.
     * @return TokenResponseDto an object containing both the access token (valid for 1 day)
     * and the refresh token (valid for 30 days) if authentication is successful.
     * @throws HttpResponseException if the username is not found or if the password is incorrect,
     *                               with an error code of UNAUTHORIZED and a message "Invalid username or password".
     */
    public TokenResponseDto authenticated(UserLoginRequestDto userLoginRequestDto) {

        try {
            log.info("{}, Authenticating user: {}", this.getClass().getSimpleName(), userLoginRequestDto.username());
            var user = userRepository.findByUsername(userLoginRequestDto.username())
                    .orElseThrow(AuthError.INVALID_USERNAME_OR_PASSWORD::exception);

            boolean authenticated = passwordEncoder.matches(userLoginRequestDto.password(), user.getPassword());

            if (!authenticated) {
                throw AuthError.INVALID_USERNAME_OR_PASSWORD.exception();
            }

            var token = jwtService.generateToken(user, 1, JwtService.TokenType.ACCESS_TOKEN);
            var refreshToken = jwtService.generateToken(user, 30, JwtService.TokenType.REFRESH_TOKEN);
            TokenResponseDto tokenResponseDto = new TokenResponseDto(token, refreshToken);

            log.info("{}, User authenticated: {}", this.getClass().getSimpleName(), userLoginRequestDto.username());
            return tokenResponseDto;
        } catch (Exception e) {
            log.error("{}, Error authenticating user: {}", this.getClass().getSimpleName(), userLoginRequestDto.username());
            throw e;
        }

    }

    /**
     * Refreshes the access token using a provided refresh token.
     *
     * @param refreshToken the refresh token used to generate a new access token.
     * @return TokenResponseDto an object containing the new access token and, if applicable,
     * a new refresh token based on the provided refresh token.
     * @throws HttpResponseException if the refresh token is invalid or expired.
     */
    public TokenResponseDto refreshToken(String refreshToken) {
        try {
            log.info("{}, Refreshing token: {}", this.getClass().getSimpleName(), refreshToken);
            TokenResponseDto tokenResponseDto = jwtService.refreshToken(refreshToken);
            log.info("{}, Token refreshed: {}", this.getClass().getSimpleName(), refreshToken);
            return tokenResponseDto;
        } catch (Exception e) {
            log.error("{}, Error refreshing token: {}", this.getClass().getSimpleName(), refreshToken);
            throw e;
        }
    }

}
