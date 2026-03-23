package com.pimentadesenvolvimento.conroledebolsaoback.service;

import com.pimentadesenvolvimento.conroledebolsaoback.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;

/**
 * Service for JWT token blacklisting and revocation.
 * <p>
 * Delegates to TokenBlacklistPort for storage implementation.
 * Supports any backend: in-memory, Redis, database, etc.
 */
@Service
@Slf4j
public class JwtBlacklistService {

    private final SecretKey key;
    private final TokenBlacklistPort tokenBlacklistPort;

    public JwtBlacklistService(JwtProperties jwtProperties, TokenBlacklistPort tokenBlacklistPort) {
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
        this.tokenBlacklistPort = tokenBlacklistPort;
    }

    private static String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            return token;
        }
    }

    public void blacklistToken(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            String jti = Optional.ofNullable(claims.getId()).orElse(hashToken(token));
            Instant expiration = claims.getExpiration().toInstant();

            if (expiration.isBefore(Instant.now())) {
                return; // Token already expired
            }

            tokenBlacklistPort.blacklist(jti, expiration);
        } catch (Exception e) {
            log.debug("Skipping blacklisting for invalid token", e);
        }
    }

    public boolean isBlacklisted(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            String jti = Optional.ofNullable(claims.getId()).orElse(hashToken(token));
            return tokenBlacklistPort.isBlacklisted(jti);
        } catch (Exception e) {
            return false;
        }
    }
}

