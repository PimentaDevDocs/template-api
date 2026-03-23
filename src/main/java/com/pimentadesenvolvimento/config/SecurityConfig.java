package com.pimentadesenvolvimento.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Development profile security configuration.
 * Extends BaseSecurityConfig with dev-specific settings (H2 console, SwaggerUI, etc).
 */
// Note: the "test" profile uses this same dev security config because @ActiveProfiles("test")
// does not activate @Profile("dev"). Tests rely on application-test.properties disabling
// rate limiting and setting required secrets, not on a separate security profile.
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Profile({"dev", "test"})
public class SecurityConfig extends BaseSecurityConfig {

    public SecurityConfig(
            JwtAuthFilter jwtAuthFilter,
            WebhookAuthFilter webhookAuthFilter,
            LoginRateLimitingFilter loginRateLimitingFilter
    ) {
        super(jwtAuthFilter, webhookAuthFilter, loginRateLimitingFilter);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        configureBasicSecurity(http);

        http.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable));

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(PathRequest.toH2Console()).permitAll()
                .requestMatchers(
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/favicon.ico",
                        "/api/auth/**",
                        "/webhook/**",
                        "/api/health/live",
                        "/api/health/ready",
                        "/api/health/startup"
                ).permitAll()
                .anyRequest().authenticated()
        );

        addCommonFilters(http);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        return devCorsConfigurationSource();
    }
}