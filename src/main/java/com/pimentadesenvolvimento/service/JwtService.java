package com.pimentadesenvolvimento.service;

import com.pimentadesenvolvimento.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class JwtService {
    private static final long DEFAULT_ACCESS_TTL = 1000 * 60 * 5;
    private static final String ROLES_CLAIM = "roles";

    private final SecretKey key;
    private final JwtProperties props;
    private final ObjectProvider<JwtBlacklistService> blacklistService;

    public JwtService(JwtProperties props, ObjectProvider<JwtBlacklistService> blacklistService) {
        this.props = props;
        this.blacklistService = blacklistService;
        this.key = Keys.hmacShaKeyFor(props.getSecret().getBytes());
    }

    public String generateAccessToken(UserDetails userDetails) {
        var roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        long expirationTime = props.getExpirationAccess() > 0 ? props.getExpirationAccess() : DEFAULT_ACCESS_TTL;
        Instant now = Instant.now();
        Instant expiresAt = now.plusMillis(expirationTime);

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(userDetails.getUsername())
                .claim(ROLES_CLAIM, roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(UserDetails userDetails) {
        long expirationTime = props.getExpirationRefresh() > 0 ? props.getExpirationRefresh() : DEFAULT_ACCESS_TTL * 12;
        Instant now = Instant.now();
        Instant expiresAt = now.plusMillis(expirationTime);

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(userDetails.getUsername())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(key)
                .compact();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenValid(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            JwtBlacklistService blacklist = blacklistService.getIfAvailable();
            return blacklist == null || !blacklist.isBlacklisted(token);
        } catch (Exception e) {
            return false;
        }
    }
}