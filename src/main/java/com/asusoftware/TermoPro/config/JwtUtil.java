package com.asusoftware.TermoPro.config;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class JwtUtil {
    private final JwtDecoder jwtDecoder;

    public boolean validateToken(String token) {
        try {
            jwtDecoder.decode(token); // dacă nu aruncă excepție, e valid
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public String getUserIdFromToken(String token) {
        Jwt decoded = jwtDecoder.decode(token);
        return decoded.getSubject(); // sau .getClaim("sub")
    }
}
