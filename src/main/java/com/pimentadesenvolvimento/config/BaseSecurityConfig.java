package com.pimentadesenvolvimento.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Base security configuration shared between dev and prod profiles.
 * Contains common patterns and helper methods for security setup.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public abstract class BaseSecurityConfig {

    protected final JwtAuthFilter jwtAuthFilter;
    protected final WebhookAuthFilter webhookAuthFilter;
    protected final LoginRateLimitingFilter loginRateLimitingFilter;

    /**
     * Add common filters to the security filter chain
     */
    protected void addCommonFilters(HttpSecurity http) throws Exception {
        http.addFilterBefore(loginRateLimitingFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(webhookAuthFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
    }

    /**
     * Configure basic session management and CORS
     */
    protected void configureBasicSecurity(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
        http.cors(Customizer.withDefaults());
        http.sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    }

    /**
     * Configure CORS for development environment (permissive)
     */
    public CorsConfigurationSource devCorsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:4200", "http://localhost:8080"));
        configuration.setAllowedMethods(Arrays.asList(
                HttpMethod.GET.name(),
                HttpMethod.POST.name(),
                HttpMethod.PUT.name(),
                HttpMethod.PATCH.name(),
                HttpMethod.DELETE.name(),
                HttpMethod.OPTIONS.name()
        ));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Configure CORS for production environment (restrictive)
     */
    public CorsConfigurationSource prodCorsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        String allowedOrigin = System.getProperty("app.allowed-origin");
        if (allowedOrigin == null || allowedOrigin.isBlank()) {
            allowedOrigin = System.getenv("APP_ALLOWED_ORIGIN");
        }
        if (allowedOrigin == null || allowedOrigin.isBlank()) {
            allowedOrigin = "https://example.com";
        }

        configuration.setAllowedOrigins(List.of(allowedOrigin));
        configuration.setAllowedMethods(Arrays.asList(
                HttpMethod.GET.name(),
                HttpMethod.POST.name(),
                HttpMethod.PUT.name(),
                HttpMethod.PATCH.name(),
                HttpMethod.DELETE.name(),
                HttpMethod.OPTIONS.name()
        ));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-XSRF-TOKEN"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
