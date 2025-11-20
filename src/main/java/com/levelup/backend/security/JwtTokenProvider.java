package com.levelup.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtTokenProvider {
    private final SecretKey key;
    private final long expirationMs;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(deriveSecureKey(secret));
        this.expirationMs = expirationMs;
    }

    private byte[] deriveSecureKey(String secret) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(secret.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm is required for JWT key derivation", ex);
        }
    }

    public String generateToken(LevelUpUserDetails userDetails) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("run", userDetails.getUser().getRun())
                .claim("perfil", userDetails.getUser().getPerfil().name())
                .claim("correo", userDetails.getUser().getCorreo())
                .claim("name", userDetails.getUser().getNombre())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusMillis(expirationMs)))
                .signWith(key)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException ex) {
            log.warn("Invalid JWT: {}", ex.getMessage());
            return false;
        }
    }

    public Claims getClaims(String token) {
        return parseClaims(token).getBody();
    }

    public String getUsername(String token) {
        return parseClaims(token).getBody().getSubject();
    }

    private Jws<Claims> parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }
}
