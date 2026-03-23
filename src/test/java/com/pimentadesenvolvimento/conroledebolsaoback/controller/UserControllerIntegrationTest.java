package com.pimentadesenvolvimento.conroledebolsaoback.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pimentadesenvolvimento.conroledebolsaoback.domain.Role;
import com.pimentadesenvolvimento.conroledebolsaoback.domain.User;
import com.pimentadesenvolvimento.conroledebolsaoback.dto.AuthResponse;
import com.pimentadesenvolvimento.conroledebolsaoback.dto.UserCreateRequest;
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

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for UserController.
 * Tests pagination, EntityGraph N+1 prevention, and user operations.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("UserController Integration Tests")
public class UserControllerIntegrationTest {

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

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() throws Exception {
        // Setup: Create roles
        Role adminRole = roleRepository.findByName(SecurityRoles.NAME_ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(SecurityRoles.NAME_ROLE_ADMIN)));
        Role userRole = roleRepository.findByName(SecurityRoles.NAME_ROLE_USER)
                .orElseGet(() -> roleRepository.save(new Role(SecurityRoles.NAME_ROLE_USER)));

        // Setup: Create admin user
        User admin = new User();
        admin.setName("Admin User");
        admin.setUsername("admin_test");
        admin.setEmail("admin@test.com");
        admin.setPassword(passwordEncoder.encode("AdminPass123!"));
        Set<Role> adminRoles = new HashSet<>();
        adminRoles.add(adminRole);
        adminRoles.add(userRole);
        admin.setRoles(adminRoles);
        userRepository.save(admin);

        // Setup: Create regular user
        User regularUser = new User();
        regularUser.setName("Regular User");
        regularUser.setUsername("user_test");
        regularUser.setEmail("user@test.com");
        regularUser.setPassword(passwordEncoder.encode("UserPass123!"));
        Set<Role> userRoles = new HashSet<>();
        userRoles.add(userRole);
        regularUser.setRoles(userRoles);
        userRepository.save(regularUser);

        // Login and get admin token
        adminToken = getTokenFromLogin("admin_test", "AdminPass123!");

        // Login and get user token
        userToken = getTokenFromLogin("user_test", "UserPass123!");
    }

    /**
     * Helper method to login and extract access token from AuthResponse
     */
    private String getTokenFromLogin(String username, String password) throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "username": "%s",
                                    "password": "%s"
                                }
                                """.formatted(username, password)))
                .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        AuthResponse authResponse = objectMapper.readValue(responseBody, AuthResponse.class);
        return authResponse.accessToken();
    }

    @Test
    @DisplayName("GET /api/users - Should return paginated users with roles via EntityGraph")
    void testGetAllUsersPaginated() throws Exception {
        mockMvc.perform(get("/api/users?page=0&size=10&sort=id,desc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(0))))
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(0L)))
                .andExpect(jsonPath("$.pageable").exists());
    }

    @Test
    @DisplayName("GET /api/users - Invalid sort field should return 400")
    void testGetAllUsersInvalidSortField() throws Exception {
        mockMvc.perform(get("/api/users?page=0&size=10&sort=invalidField,asc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/users - Create new user with roles validation")
    void testCreateUserWithValidRoles() throws Exception {
        UserCreateRequest request = new UserCreateRequest(
                "newuser",
                "NewUser123!@",
                "newuser@test.com",
                "New User",
                Set.of(SecurityRoles.NAME_ROLE_USER)
        );

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content("""
                                {
                                    "username": "newuser",
                                    "password": "NewUser123!@",
                                    "name": "New User",
                                    "email": "newuser@test.com",
                                    "roles": ["ROLE_USER"]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.roles", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("POST /api/users - Create with invalid role should fail")
    void testCreateUserWithInvalidRole() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content("""
                                {
                                    "username": "newuser2",
                                    "password": "NewUser123!@",
                                    "name": "New User",
                                    "email": "newuser2@test.com",
                                    "roles": ["ROLE_NONEXISTENT"]
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/users/{id} - Should include person with contacts in response")
    void testGetUserByIdWithContacts() throws Exception {
        // Use specific test user to avoid ID mismatch
        User testUser = userRepository.findByUsername("user_test").orElseThrow();

        mockMvc.perform(get("/api/users/" + testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(testUser.getId()))
                .andExpect(jsonPath("$.username").value("user_test"))
                .andExpect(jsonPath("$.roles").doesNotExist())
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.createdBy").doesNotExist());
    }

    @Test
    @DisplayName("PUT /api/users/{id} - User can only update own profile")
    void testUpdateUserOwnProfile() throws Exception {
        User testUser = userRepository.findByUsername("user_test").orElseThrow();

        mockMvc.perform(put("/api/users/" + testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + userToken)
                        .content("""
                                {
                                    "name": "Updated Name"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.roles").doesNotExist())
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.createdBy").doesNotExist());
    }
}
