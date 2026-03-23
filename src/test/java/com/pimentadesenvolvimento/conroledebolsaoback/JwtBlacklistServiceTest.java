package com.pimentadesenvolvimento.conroledebolsaoback;

import com.pimentadesenvolvimento.conroledebolsaoback.config.JwtProperties;
import com.pimentadesenvolvimento.conroledebolsaoback.service.InMemoryTokenBlacklist;
import com.pimentadesenvolvimento.conroledebolsaoback.service.JwtBlacklistService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JwtBlacklistServiceTest {

    @Test
    void whenTokenBlacklisted_thenIsBlacklistedReturnsTrue() {
        JwtProperties props = new JwtProperties();
        props.setSecret("01234567890123456789012345678901");

        InMemoryTokenBlacklist inMemoryBlacklist = new InMemoryTokenBlacklist();
        JwtBlacklistService blacklistService = new JwtBlacklistService(props, inMemoryBlacklist);

        SecretKey key = Keys.hmacShaKeyFor(props.getSecret().getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject("user")
                .id("test-jti")
                .expiration(Date.from(Instant.now().plusSeconds(3600)))
                .signWith(key)
                .compact();

        blacklistService.blacklistToken(token);
        assertThat(blacklistService.isBlacklisted(token)).isTrue();
    }
}

