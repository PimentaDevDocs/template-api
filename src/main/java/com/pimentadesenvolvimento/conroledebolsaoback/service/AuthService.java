package com.pimentadesenvolvimento.conroledebolsaoback.service;

import com.pimentadesenvolvimento.conroledebolsaoback.domain.User;
import com.pimentadesenvolvimento.conroledebolsaoback.dto.AuthResponse;
import com.pimentadesenvolvimento.conroledebolsaoback.dto.LoginRequest;
import com.pimentadesenvolvimento.conroledebolsaoback.dto.UserResponse;
import com.pimentadesenvolvimento.conroledebolsaoback.exception.BusinessException;
import com.pimentadesenvolvimento.conroledebolsaoback.mapper.UserMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final JwtService jwtService;
    private final AuthenticationManager authManager;
    private final AppUserDetailsService userDetailsService;
    private final SecurityService securityService;
    private final UserMapper userMapper;
    private final JwtBlacklistService jwtBlacklistService;

    public AuthService(
            JwtService jwtService,
            @Lazy AuthenticationManager authManager,
            AppUserDetailsService userDetailsService,
            SecurityService securityService,
            UserMapper userMapper,
            JwtBlacklistService jwtBlacklistService
    ) {
        this.jwtService = jwtService;
        this.authManager = authManager;
        this.userDetailsService = userDetailsService;
        this.securityService = securityService;
        this.userMapper = userMapper;
        this.jwtBlacklistService = jwtBlacklistService;
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return new AuthResponse(
                jwtService.generateAccessToken(userDetails),
                jwtService.generateRefreshToken(userDetails)
        );
    }

    @Transactional(readOnly = true)
    public AuthResponse refresh(String refreshToken) {
        if (!jwtService.isTokenValid(refreshToken)) {
            throw new BusinessException("Refresh token is invalid or expired.");
        }
        String username = jwtService.extractUsername(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return new AuthResponse(
                jwtService.generateAccessToken(userDetails),
                jwtService.generateRefreshToken(userDetails)
        );
    }

    /**
     * Returns the currently authenticated user details as DTO.
     */
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        User user = securityService.getLoggedUser();
        return userMapper.toResponse(user);
    }

    public void logout(String accessToken, String refreshToken) {
        if (accessToken != null && !accessToken.isBlank()) {
            jwtBlacklistService.blacklistToken(accessToken);
        }
        if (refreshToken != null && !refreshToken.isBlank()) {
            jwtBlacklistService.blacklistToken(refreshToken);
        }
    }
}