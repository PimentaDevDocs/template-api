package com.pimentadesenvolvimento.service;

import com.pimentadesenvolvimento.dto.AuthResponse;
import com.pimentadesenvolvimento.exception.BusinessException;
import com.pimentadesenvolvimento.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private AppUserDetailsService userDetailsService;

    @Mock
    private SecurityService securityService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtBlacklistService jwtBlacklistService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                jwtService,
                authenticationManager,
                userDetailsService,
                securityService,
                userMapper,
                jwtBlacklistService
        );
    }

    @Test
    void testRefreshWhenTokenIsInvalid() {
        when(jwtService.isTokenValid("invalid-token")).thenReturn(false);

        assertThrows(BusinessException.class, () -> {
            authService.refresh("invalid-token");
        });

        verify(jwtService).isTokenValid("invalid-token");
    }

    @Test
    void testRefreshWhenTokenIsValid() {
        UserDetails userDetails = mock(UserDetails.class);
        when(jwtService.isTokenValid("valid-token")).thenReturn(true);
        when(jwtService.extractUsername("valid-token")).thenReturn("testuser");
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(jwtService.generateAccessToken(userDetails)).thenReturn("new-access");
        when(jwtService.generateRefreshToken(userDetails)).thenReturn("new-refresh");

        AuthResponse result = authService.refresh("valid-token");

        assertNotNull(result);
        assertEquals("new-access", result.accessToken());
        assertEquals("new-refresh", result.refreshToken());
        verify(jwtService).generateAccessToken(userDetails);
    }

    @Test
    void testLogoutWithValidTokens() {
        authService.logout("access-token", "refresh-token");

        verify(jwtBlacklistService).blacklistToken("access-token");
        verify(jwtBlacklistService).blacklistToken("refresh-token");
    }

    @Test
    void testLogoutWithNullTokens() {
        assertDoesNotThrow(() -> {
            authService.logout(null, null);
        });

        verify(jwtBlacklistService, never()).blacklistToken(anyString());
    }
}
