package com.pimentadesenvolvimento.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of TokenBlacklistPort.
 * Suitable for single-instance deployments.
 * Data is NOT persisted and will be lost on application restart.
 */
@Service
@Slf4j
@ConditionalOnMissingBean(name = "redisTokenBlacklist")
public class InMemoryTokenBlacklist implements TokenBlacklistPort {

    private final Map<String, Instant> blacklist = new ConcurrentHashMap<>();

    @Override
    public void blacklist(String jti, Instant expiration) {
        blacklist.put(jti, expiration);
        log.debug("Token blacklisted (jti={})", jti);
    }

    @Override
    public boolean isBlacklisted(String jti) {
        Instant expiry = blacklist.get(jti);
        return expiry != null && Instant.now().isBefore(expiry);
    }

    @Scheduled(fixedDelayString = "${app.jwt.blacklist.cleanup-interval:300000}")
    void cleanupExpiredEntries() {
        Instant now = Instant.now();
        blacklist.entrySet().removeIf(entry -> now.isAfter(entry.getValue()));
        log.debug("Cleaned up expired blacklist entries. Current size: {}", blacklist.size());
    }
}
