package com.pimentadesenvolvimento.service;

import com.pimentadesenvolvimento.domain.User;
import com.pimentadesenvolvimento.dto.UserCreateRequest;
import com.pimentadesenvolvimento.dto.UserResponse;
import com.pimentadesenvolvimento.dto.UserUpdateRequest;
import com.pimentadesenvolvimento.exception.ResourceNotFoundException;
import com.pimentadesenvolvimento.exception.UserAlreadyExistsException;
import com.pimentadesenvolvimento.mapper.UserMapper;
import com.pimentadesenvolvimento.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SecurityService securityService;

    @Mock
    private ObjectProvider<UserEventPublisher> userEventPublisher;

    @Mock
    private ObjectProvider<AuditLogService> auditLogService;

    @Mock
    private PasswordPolicyService passwordPolicyService;

    @Mock
    private RoleResolverService roleResolverService;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(
                userRepository,
                userMapper,
                passwordEncoder,
                securityService,
                userEventPublisher,
                auditLogService,
                passwordPolicyService,
                roleResolverService
        );
    }

    @Test
    void testCreateWhenUsernameAlreadyExists() {
        UserCreateRequest request = new UserCreateRequest("existing", "password", "test@test.com", "Test User", Set.of());
        when(userRepository.findByUsername("existing")).thenReturn(Optional.of(new User()));

        assertThrows(UserAlreadyExistsException.class, () -> {
            userService.create(request);
        });

        verify(userRepository).findByUsername("existing");
    }

    @Test
    void testCreateWhenEmailAlreadyExists() {
        UserCreateRequest request = new UserCreateRequest("newuser", "password", "existing@test.com", "Test User", Set.of());
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> {
            userService.create(request);
        });

        verify(userRepository).existsByEmail("existing@test.com");
    }

    @Test
    void testCreateWithValidData() {
        UserCreateRequest request = new UserCreateRequest("newuser", "MySecure1@Pass", "test@test.com", "Test User", Set.of());
        User newUser = new User();
        newUser.setId(1L);
        newUser.setUsername("newuser");
        UserResponse response = new UserResponse(1L, "Test User", "newuser", "test@test.com", Set.of(), null, null, null);

        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userRepository.existsByEmail("test@test.com")).thenReturn(false);
        when(passwordEncoder.encode("MySecure1@Pass")).thenReturn("encoded");
        when(roleResolverService.resolve(Set.of())).thenReturn(Set.of());
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        when(userMapper.toResponse(newUser)).thenReturn(response);

        UserResponse result = userService.create(request);

        assertNotNull(result);
        assertEquals("newuser", result.username());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testFindByIdWhenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            userService.findById(1L);
        });

        verify(userRepository).findById(1L);
    }

    @Test
    void testDeleteWhenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            userService.delete(1L);
        });

        verify(userRepository).findById(1L);
    }

    @Test
    void testDeleteWithValidId() {
        User user = new User();
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(securityService.getLoggedUser()).thenReturn(user);

        userService.delete(1L);

        verify(userRepository).save(user);
    }

    @Test
    void testUpdateWhenUserNotFound() {
        UserUpdateRequest request = new UserUpdateRequest(null, null, "email@test.com", "name", null);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            userService.update(1L, request);
        });

        verify(userRepository).findById(1L);
    }
}
