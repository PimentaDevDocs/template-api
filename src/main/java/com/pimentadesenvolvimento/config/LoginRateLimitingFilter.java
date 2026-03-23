package com.pimentadesenvolvimento.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@ConditionalOnProperty(prefix = "app.rate-limit", name = "enabled", havingValue = "true", matchIfMissing = true)
public class LoginRateLimitingFilter extends OncePerRequestFilter {

    private static final String LOGIN_PATH = "/api/auth/login";

    private final RateLimitingProperties properties;

    // Keeps a sliding window of login attempts per client (IP + endpoint).
    // Cleaned periodically to prevent unbounded memory growth.
    private final Map<String, RequestCounter> counters = new ConcurrentHashMap<>();

    public LoginRateLimitingFilter(RateLimitingProperties properties) {
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();
        if (!LOGIN_PATH.equals(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientKey = buildClientKey(request);
        long now = Instant.now().toEpochMilli();

        RequestCounter counter = counters.computeIfAbsent(clientKey, k -> new RequestCounter(now));

        synchronized (counter) {
            long windowMillis = properties.getWindow().toMillis();
            if (now - counter.windowStart > windowMillis) {
                counter.windowStart = now;
                counter.count.set(0);
            }

            int current = counter.count.incrementAndGet();
            if (current > properties.getMaxAttempts()) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"" + Messages.Security.LOGIN_RATE_LIMIT_EXCEEDED + "\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    @Scheduled(fixedDelayString = "${app.rate-limit.cleanup-interval:300000}")
    void cleanupStaleCounters() {
        long now = Instant.now().toEpochMilli();
        long cleanupMillis = properties.getCleanupInterval().toMillis();
        counters.entrySet().removeIf(entry -> now - entry.getValue().windowStart > cleanupMillis);
    }

    private String buildClientKey(HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        return ip + ":" + LOGIN_PATH;
    }

    private static final class RequestCounter {
        final AtomicInteger count;
        volatile long windowStart;

        RequestCounter(long windowStart) {
            this.windowStart = windowStart;
            this.count = new AtomicInteger(0);
        }
    }
}

