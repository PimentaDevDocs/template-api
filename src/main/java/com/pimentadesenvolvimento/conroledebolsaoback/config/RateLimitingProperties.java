package com.pimentadesenvolvimento.conroledebolsaoback.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConfigurationProperties(prefix = "app.rate-limit")
@Getter
@Setter
public class RateLimitingProperties {

    /**
     * Whether rate limiting is enabled.
     */
    private boolean enabled = true;

    /**
     * Maximum number of login attempts per sliding window.
     */
    private int maxAttempts = 20;

    /**
     * Window duration for counting login attempts.
     */
    private Duration window = Duration.ofMinutes(1);

    /**
     * How often to cleanup stale rate limit data.
     */
    private Duration cleanupInterval = Duration.ofMinutes(5);
}
