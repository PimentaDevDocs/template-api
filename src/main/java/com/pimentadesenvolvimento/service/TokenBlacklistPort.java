package com.pimentadesenvolvimento.service;

import java.time.Instant;

/**
 * Port interface for token blacklisting abstraction.
 * Allows different implementations (in-memory, Redis, database, etc).
 */
public interface TokenBlacklistPort {
    void blacklist(String jti, Instant expiration);

    boolean isBlacklisted(String jti);
}
