package com.white.backend.authentication.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.white.backend.authentication.dto.response.TokenResponseDto;
import com.white.backend.authentication.entity.User;
import com.white.backend.authentication.error.AuthError;
import com.white.backend.authentication.repository.UserRepository;
import com.white.backend.shared.exception.HttpResponseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

/**
 * JwtService for generating and handling JWT tokens.
 * Provides functionality for generating access and refresh tokens, verifying tokens,
 * and refreshing tokens based on user information and security requirements.
 * This service relies on the {@link UserRepository} to retrieve user data
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtService {

    @Value("${jwt.signer-key}")
    private String signerKey;

    private final UserRepository userRepository;

    /**
     * Generates a JWT for a given user. Supports creation of either an access or refresh token,
     * each with different expiration policies.
     *
     * @param user          The {@link User} entity for whom the token is generated.
     * @param expirationDay The duration (in days) until the token expires.
     * @param tokenType     The type of token to create, either ACCESS_TOKEN or REFRESH_TOKEN.
     *                      Access tokens are signed with HS256, and refresh tokens with HS512.
     * @return A String representation of the generated JWT.
     * @throws HttpResponseException Exception if there is an error during token creation.
     */
    public String generateToken(User user, int expirationDay, TokenType tokenType) {

        Date now = new Date();

        Instant nowInstant = now.toInstant();

        Instant expirationInstant = nowInstant.plus(expirationDay, ChronoUnit.DAYS);

        Date expirationTime = Date.from(expirationInstant);

        JWSHeader header;

        if (tokenType == TokenType.ACCESS_TOKEN) {

            header = new JWSHeader(JWSAlgorithm.HS256);

        } else {

            header = new JWSHeader(JWSAlgorithm.HS512);

        }

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("dev-white2077")
                .issueTime(now)
                .claim("avatar", user.getAvatar())
                .claim("name", user.getName())
                .claim("email", user.getEmail())
                .expirationTime(expirationTime)
                .jwtID(UUID.randomUUID().toString())
                .subject(user.getUsername())
                .audience(user.getUsername())
                .claim("scope", buildScope(user))
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try {

            jwsObject.sign(new MACSigner(signerKey.getBytes()));

            return jwsObject.serialize();

        } catch (JOSEException e) {

            log.error("Cannot Create JWT", e);

            throw AuthError.UNAUTHORIZED.exception();

        }
    }

    /**
     * Refreshes an access token using a valid refresh token. Extracts user information
     * from the provided refresh token and, if valid, issues a new access token.
     *
     * @param refreshToken The refresh token used for renewing access.
     * @return {@link TokenResponseDto} containing the new access token and the original refresh token.
     * @throws HttpResponseException If the refresh token is invalid or expired.
     */
    public TokenResponseDto refreshToken(String refreshToken) {

        try {

            log.info("{}, refresh token: {}", this.getClass().getSimpleName(), refreshToken);

            String username = decodeJwt(refreshToken, MacAlgorithm.HS512).getSubject();

            User user = userRepository.findByUsername(username).orElseThrow((AuthError.INVALID_USERNAME_OR_PASSWORD::exception));

            TokenResponseDto tokenResponseDto = new TokenResponseDto(generateToken(user, 1, TokenType.ACCESS_TOKEN), refreshToken);

            log.info("{}, token refreshed: {}", this.getClass().getSimpleName(), refreshToken);

            return tokenResponseDto;

        } catch (Exception e) {

            log.error("{}, Error while refresh token: {}", this.getClass().getSimpleName(), refreshToken);

            throw e;

        }
    }

    /**
     * Decodes a JWT token using the specified MAC algorithm. Verifies the token's integrity
     * based on the provided algorithm and extracts claims data.
     *
     * @param token     The JWT token to decode.
     * @param algorithm The MAC algorithm to use for decoding (e.g., HS256, HS512).
     * @return {@link Jwt} object representing the decoded token with user claims.
     */
    public Jwt decodeJwt(String token, MacAlgorithm algorithm) {

        SecretKeySpec secretKeySpec = new SecretKeySpec(signerKey.getBytes(), algorithm.getName());

        return NimbusJwtDecoder
                .withSecretKey(secretKeySpec)
                .macAlgorithm(algorithm)
                .build()
                .decode(token);

    }

    /**
     * Constructs the scope (roles or permissions) for the JWT token based on the user's role.
     *
     * @param user the user.
     * @return a space-separated string representing the user's scope.
     */
    private String buildScope(User user) {

        StringJoiner stringJoiner = new StringJoiner(" ");

        if (user.getRole() != null) {

            stringJoiner.add(user.getRole().name());

        }

        return stringJoiner.toString();

    }

    /**
     * Enum representing different types of tokens. Used to differentiate between
     * access tokens (for immediate API access) and refresh tokens (for renewing access tokens).
     */
    public enum TokenType {

        ACCESS_TOKEN,

        REFRESH_TOKEN
    }

    //    user if stored token in database or redis
//    public boolean introspectJWT(String token) throws JOSEException, ParseException {
//        boolean invalid = true;
//        try {
//            verifyToken(token);
//        } catch (RuntimeException e) {
//            invalid = false;
//        }
//        return invalid;
//    }

//    private void verifyToken(String token) throws JOSEException, ParseException {
//
//        JWSVerifier verifier = new MACVerifier(signerKey.getBytes());
//
//        SignedJWT signedJWT = SignedJWT.parse(token);
//
//        Date expiredDate = signedJWT.getJWTClaimsSet().getExpirationTime();
//
//        var verified = signedJWT.verify(verifier);
//        if (!(verified && expiredDate.after(new Date()))) {
//            throw new RuntimeException("String.valueOf(ErrorCode.UNAUTHENTICATED)");
//        }
//        //check token in database or redis
//    }

}

