package com.pimentadesenvolvimento.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pimentadesenvolvimento.dto.ErrorResponse;
import com.pimentadesenvolvimento.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String path = request.getRequestURI();
        if (path.startsWith("/h2-console")
                || path.startsWith("/favicon.ico")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/swagger-ui.html")
                || path.startsWith("/api/auth/login")
                || path.startsWith("/api/auth/register")
                || path.startsWith("/webhook")) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = resolveToken(request);
        if (token == null || token.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            if (!jwtService.isTokenValid(token)) {
                setErrorResponse(request, response, HttpStatus.UNAUTHORIZED, Messages.Security.JWT_SECRET_INVALID);
                return;
            }

            Claims claims = jwtService.extractAllClaims(token);
            String username = claims.getSubject();
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                Collection<? extends GrantedAuthority> authorities = Collections.emptyList();
                Object rolesObj = claims.get("roles");
                if (rolesObj instanceof java.util.List<?> rolesList) {
                    authorities = rolesList.stream()
                            .filter(o -> o instanceof String)
                            .map(o -> new SimpleGrantedAuthority((String) o))
                            .collect(Collectors.toList());
                }
                var authToken = new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        authorities
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (ExpiredJwtException ex) {
            setErrorResponse(request, response, HttpStatus.UNAUTHORIZED, Messages.Security.JWT_SECRET_EXPIRED);
            return;
        } catch (MalformedJwtException | SignatureException ex) {
            setErrorResponse(request, response, HttpStatus.UNAUTHORIZED, Messages.Security.JWT_SECRET_INVALID);
            return;
        } catch (Exception ex) {
            setErrorResponse(request, response, HttpStatus.UNAUTHORIZED, Messages.Security.JWT_SECRET_FAILURE + ex.getMessage());
            return;
        }
        filterChain.doFilter(request, response);
    }

    /**
     * Resolve JWT token from Authorization header (Bearer) or HttpOnly cookie.
     */
    private String resolveToken(HttpServletRequest request) {
        final String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String bearerToken = authHeader.substring(7).replaceAll("\\s+", "");
            if (!bearerToken.isEmpty()) {
                return bearerToken;
            }
        }

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("ACCESS_TOKEN".equals(cookie.getName())) {
                    String value = cookie.getValue();
                    if (value != null && !value.isBlank()) {
                        return value.trim();
                    }
                }
            }
        }
        return null;
    }

    private void setErrorResponse(HttpServletRequest request, HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ErrorResponse errorDetails = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                null
        );
        response.getWriter().write(objectMapper.writeValueAsString(errorDetails));
    }
}