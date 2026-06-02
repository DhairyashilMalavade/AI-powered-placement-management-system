package com.dhairya.Placement_management_system.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private static final String SECRET = "dev-secret-key-min-256-bits-long-for-hs256-algorithm";
    private static final long EXPIRATION_MS = 86400000;

    private JwtTokenProvider provider;

    @BeforeEach
    void setUp() {
        provider = new JwtTokenProvider(SECRET, EXPIRATION_MS);
    }

    @Test
    void isTokenActive_WithActiveTrue_ReturnsTrue() {
        String token = provider.generateToken(UUID.randomUUID(), "STUDENT", true);
        assertThat(provider.isTokenActive(token)).isTrue();
    }

    @Test
    void isTokenActive_WithActiveFalse_ReturnsFalse() {
        String token = provider.generateToken(UUID.randomUUID(), "STUDENT", false);
        assertThat(provider.isTokenActive(token)).isFalse();
    }

    @Test
    void isTokenActive_WithoutActiveClaim_ReturnsTrueForBackwardCompat() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
            .subject(UUID.randomUUID().toString())
            .claim("role", "STUDENT")
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
            .signWith(key)
            .compact();
        assertThat(provider.isTokenActive(token)).isTrue();
    }

    @Test
    void isTokenActive_WithInvalidToken_ReturnsFalse() {
        assertThat(provider.isTokenActive("invalid.jwt.token")).isFalse();
    }

    @Test
    void isTokenActive_WithTamperedToken_ReturnsFalse() {
        String token = provider.generateToken(UUID.randomUUID(), "STUDENT", true);
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";
        assertThat(provider.isTokenActive(tampered)).isFalse();
    }
}
