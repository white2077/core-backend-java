package com.white.backend.authentication.config;

import com.white.backend.authentication.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtDecoder implements org.springframework.security.oauth2.jwt.JwtDecoder {

    private final JwtService JwtService;

    @Override
    public Jwt decode(String token) throws JwtException {

//        try {
//            var response = JwtUtil.introspectJWT(token);
//            if (!response)
//                throw new JwtException("Token invalid");
//        } catch (JOSEException | ParseException e) {
//            throw new JwtException(e.getMessage());
//        }

        return JwtService.decodeJwt(token, MacAlgorithm.HS256);

    }
}
