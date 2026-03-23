package com.pimentadesenvolvimento.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Production profile security configuration.
 * Extends BaseSecurityConfig with prod-specific hardened settings (HSTS, CSP, etc).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Profile("prod")
public class ProdSecurityConfig extends BaseSecurityConfig {

    public ProdSecurityConfig(
            JwtAuthFilter jwtAuthFilter,
            WebhookAuthFilter webhookAuthFilter,
            LoginRateLimitingFilter loginRateLimitingFilter
    ) {
        super(jwtAuthFilter, webhookAuthFilter, loginRateLimitingFilter);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        configureBasicSecurity(http);

        // CSRF with cookie repository (production security best practice)
        http.csrf(csrf -> csrf
                .csrfTokenRepository(org.springframework.security.web.csrf.CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringRequestMatchers("/api/auth/**", "/webhook/**")
        );

        // Enforce HTTPS
        http.requiresChannel(channel -> channel.anyRequest().requiresSecure());

        // Hardened security headers
        http.headers(headers -> headers
                .httpStrictTransportSecurity(hsts -> hsts
                        .includeSubDomains(true)
                        .preload(true)
                        .maxAgeInSeconds(31536000))
                .contentSecurityPolicy(csp -> csp
                        .policyDirectives("default-src 'self'; frame-ancestors 'none'; form-action 'self'; script-src 'self'; style-src 'self' 'unsafe-inline';"))
                .referrerPolicy(referrer -> referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.SAME_ORIGIN))
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
        );

        // Strict authorization rules for production
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/swagger-ui/**"
                ).denyAll()
                .requestMatchers(
                        "/favicon.ico",
                        "/api/auth/**",
                        "/webhook/**"
                ).permitAll()
                .requestMatchers("/api/health/live", "/api/health/ready", "/api/health/startup").permitAll()  // Public health checks only
                .anyRequest().authenticated()
        );

        addCommonFilters(http);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        return prodCorsConfigurationSource();
    }
}

