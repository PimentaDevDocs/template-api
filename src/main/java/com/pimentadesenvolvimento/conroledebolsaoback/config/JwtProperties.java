package com.pimentadesenvolvimento.conroledebolsaoback.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    @Setter
    private String secret;

    private long expirationAccess;
    private long expirationRefresh;

    @PostConstruct
    void validate() {
        if (secret == null || secret.isBlank() || secret.equals("${JWT_SECRET}")) {
            String envSecret = System.getenv("JWT_SECRET");
            if (envSecret != null && !envSecret.isBlank()) {
                this.secret = envSecret;
            } else {
                throw new IllegalStateException(Messages.Security.JWT_SECRET_MISSING);
            }
        }

        if (secret.length() < 32) {
            throw new IllegalStateException(Messages.Security.JWT_SECRET_WEAK);
        }
    }
}