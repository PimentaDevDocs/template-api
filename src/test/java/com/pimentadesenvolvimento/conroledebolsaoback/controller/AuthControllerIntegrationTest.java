package com.pimentadesenvolvimento.conroledebolsaoback.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pimentadesenvolvimento.conroledebolsaoback.domain.Role;
import com.pimentadesenvolvimento.conroledebolsaoback.domain.User;
import com.pimentadesenvolvimento.conroledebolsaoback.dto.AuthResponse;
import com.pimentadesenvolvimento.conroledebolsaoback.repository.RoleRepository;
import com.pimentadesenvolvimento.conroledebolsaoback.repository.UserRepository;
import com.pimentadesenvolvimento.conroledebolsaoback.security.SecurityRoles;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AuthController.
 * Tests authentication, token refresh, JWT security, and refresh token via httpOnly cookie.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("AuthController Integration Tests")
public class AuthControllerIntegrationTest {

    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_PASSWORD = "TestPassword123!@";
    private static final String TEST_EMAIL = "testuser@test.com";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        // Setup: Create roles
        Role userRole = roleRepository.findByName(SecurityRoles.NAME_ROLE_USER)
                .orElseGet(() -> roleRepository.save(new Role(SecurityRoles.NAME_ROLE_USER)));

        // Setup: Create test user
        User testUser = new User();
        testUser.setName("Test User");
        testUser.setUsername(TEST_USERNAME);
        testUser.setEmail(TEST_EMAIL);
        testUser.setPassword(passwordEncoder.encode(TEST_PASSWORD));
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        testUser.setRoles(roles);
        userRepository.save(testUser);
    }

    @Test
    @DisplayName("POST /api/auth/login - Successful login returns access and refresh tokens")
    void testLoginSuccess() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "username": "%s",
                                    "password": "%s"
                                }
                                """.formatted(TEST_USERNAME, TEST_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(header().exists("Set-Cookie"))
                .andReturn();

        // Verify httpOnly cookie is set
        String setCookieHeader = result.getResponse().getHeader("Set-Cookie");
        assert setCookieHeader.contains("HttpOnly");
    }

    @Test
    @DisplayName("POST /api/auth/login - Invalid credentials returns 401")
    void testLoginInvalidCredentials() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "username": "%s",
                                    "password": "WrongPassword123!"
                                }
                                """.formatted(TEST_USERNAME)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", containsString("Invalid credentials")));
    }

    @Test
    @DisplayName("POST /api/auth/refresh - Requires refresh token in httpOnly cookie")
    void testRefreshTokenViaHttpOnlyCookie() throws Exception {
        // First, login to get tokens
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "username": "%s",
                                    "password": "%s"
                                }
                                """.formatted(TEST_USERNAME, TEST_PASSWORD)))
                .andExpect(status().isOk())
                .andReturn();

        // Try refresh with cookie
        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(loginResult.getResponse().getCookies())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    @DisplayName("POST /api/auth/refresh - Query parameter token is REJECTED for security")
    void testRefreshTokenViaQueryParamRejected() throws Exception {
        mockMvc.perform(post("/api/auth/refresh?token=malicious_token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("httpOnly cookie")));
    }

    @Test
    @DisplayName("GET /api/auth/me - Returns current authenticated user")
    void testGetCurrentUser() throws Exception {
        // Login first
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "username": "%s",
                                    "password": "%s"
                                }
                                """.formatted(TEST_USERNAME, TEST_PASSWORD)))
                .andExpect(status().isOk())
                .andReturn();

        // Deserialize AuthResponse to extract accessToken
        String responseBody = loginResult.getResponse().getContentAsString();
        AuthResponse authResponse = objectMapper.readValue(responseBody, AuthResponse.class);
        String accessToken = authResponse.accessToken();

        // Get current user using token
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(TEST_USERNAME))
                .andExpect(jsonPath("$.email").value(TEST_EMAIL));
    }

    @Test
    @DisplayName("POST /api/auth/logout - Invalidates tokens and clears cookies")
    void testLogout() throws Exception {
        // Login first
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "username": "%s",
                                    "password": "%s"
                                }
                                """.formatted(TEST_USERNAME, TEST_PASSWORD)))
                .andExpect(status().isOk())
                .andReturn();

        // Logout
        mockMvc.perform(post("/api/auth/logout")
                        .cookie(loginResult.getResponse().getCookies())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andExpect(header().exists("Set-Cookie"));  // Cookies cleared
    }

    @Test
    @DisplayName("POST /api/auth/login - Request body validation")
    void testLoginMissingUsername() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "password": "%s"
                                }
                                """.formatted(TEST_PASSWORD)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("validation")));
    }

    @Test
    @DisplayName("POST /api/auth/refresh - Without token returns 400")
    void testRefreshWithoutToken() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Refresh token")));
    }
}
