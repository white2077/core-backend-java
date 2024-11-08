package com.white.backend.auth.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.white.backend.auth.dto.response.TokenResponseDto;
import com.white.backend.auth.entity.User;
import com.white.backend.auth.error.AuthError;
import com.white.backend.auth.repository.UserRepository;
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
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtService {

    @Value("${jwt.signer-key}")
    private String signerKey;
    private final UserRepository userRepository;

    /**
     * Generates a JWT token for a given user with specified expiration and token type.
     *
     * @param user          the user for whom the token is being generated.
     * @param expirationDay the number of days before the token expires.
     * @param tokenType     the type of token, either ACCESS_TOKEN or REFRESH_TOKEN.
     * @return a JWT token as a String.
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
     * Refreshes the access token using the provided refresh token. Decodes the refresh token to extract
     * user information and generates a new access token if the refresh token is valid.
     *
     * @param refreshToken the refresh token used for generating a new access token.
     * @return a TokenResponseDto containing the new access token and the original refresh token.
     * @throws HttpResponseException if the refresh token is invalid or expired.
     */
    public TokenResponseDto refreshToken(String refreshToken) {
        try {
            String username = decodeJwt(refreshToken, MacAlgorithm.HS512).getSubject();
            var user = userRepository.findByUsername(username).orElseThrow((AuthError.INVALID_USERNAME_OR_PASSWORD::exception));
            return new TokenResponseDto(generateToken(user, 1, TokenType.ACCESS_TOKEN), refreshToken);
        } catch (Exception e) {
            throw AuthError.INVALID_TOKEN.exception();
        }
    }

    /**
     * Decodes the JWT token using the provided algorithm. Decodes the token to extract
     * user information and verify the token's integrity.
     *
     * @param algorithm the algorithm used to decode the token.
     * @param token     the JWT token to be decoded.
     * @return a Jwt object representing the decoded token.
     */
    public Jwt decodeJwt(String token, MacAlgorithm algorithm) {
        SecretKeySpec secretKeySpec = new SecretKeySpec(signerKey.getBytes(), algorithm.getName());
        return NimbusJwtDecoder
                .withSecretKey(secretKeySpec)
                .macAlgorithm(algorithm)
                .build().decode(token);
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
     * Enum representing the types of tokens: ACCESS_TOKEN or REFRESH_TOKEN.
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

