package com.pimentadesenvolvimento.conroledebolsaoback.controller;

import com.pimentadesenvolvimento.conroledebolsaoback.config.Messages;
import com.pimentadesenvolvimento.conroledebolsaoback.dto.AuthResponse;
import com.pimentadesenvolvimento.conroledebolsaoback.dto.LoginRequest;
import com.pimentadesenvolvimento.conroledebolsaoback.dto.UserResponse;
import com.pimentadesenvolvimento.conroledebolsaoback.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {

    private final AuthService service;
    private final Environment environment;

    public AuthController(AuthService service, Environment environment) {
        this.service = service;
        this.environment = environment;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        AuthResponse auth = service.login(request);
        addAuthCookies(response, auth);
        return auth;
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(
            @CookieValue(name = Messages.Security.REFRESH_TOKEN_COOKIE, required = false) String refreshTokenCookie,
            HttpServletResponse response
    ) {
        if (refreshTokenCookie == null || refreshTokenCookie.isBlank()) {
            throw new IllegalArgumentException(
                    "Refresh token must be provided via secure httpOnly cookie. Query parameters are not accepted for security reasons."
            );
        }
        AuthResponse auth = service.refresh(refreshTokenCookie);
        addAuthCookies(response, auth);
        return auth;
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public UserResponse me() {
        return service.getCurrentUser();
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(
            @CookieValue(name = Messages.Security.ACCESS_TOKEN_COOKIE, required = false) String accessTokenCookie,
            @CookieValue(name = Messages.Security.REFRESH_TOKEN_COOKIE, required = false) String refreshTokenCookie,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            HttpServletResponse response
    ) {
        String accessToken = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            accessToken = authorizationHeader.substring(7);
        } else if (accessTokenCookie != null) {
            accessToken = accessTokenCookie;
        }

        service.logout(accessToken, refreshTokenCookie);
        clearAuthCookies(response);
    }

    private void addAuthCookies(HttpServletResponse response, AuthResponse auth) {
        boolean secure = Arrays.asList(environment.getActiveProfiles()).contains("prod");
        String sameSite = secure ? "Strict" : "Lax";

        ResponseCookie accessCookie = ResponseCookie.from(Messages.Security.ACCESS_TOKEN_COOKIE, auth.accessToken())
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path("/")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from(Messages.Security.REFRESH_TOKEN_COOKIE, auth.refreshToken())
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path("/api/auth")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    }

    private void clearAuthCookies(HttpServletResponse response) {
        boolean secure = Arrays.asList(environment.getActiveProfiles()).contains("prod");
        String sameSite = secure ? "Strict" : "Lax";

        ResponseCookie accessCookie = ResponseCookie.from(Messages.Security.ACCESS_TOKEN_COOKIE, "")
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path("/")
                .maxAge(0)
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from(Messages.Security.REFRESH_TOKEN_COOKIE, "")
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path("/api/auth")
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    }
}