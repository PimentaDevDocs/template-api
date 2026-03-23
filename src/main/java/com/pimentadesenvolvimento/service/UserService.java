package com.pimentadesenvolvimento.service;

import com.pimentadesenvolvimento.config.Messages;
import com.pimentadesenvolvimento.domain.Role;
import com.pimentadesenvolvimento.domain.User;
import com.pimentadesenvolvimento.dto.UserCreateRequest;
import com.pimentadesenvolvimento.dto.UserResponse;
import com.pimentadesenvolvimento.dto.UserUpdateRequest;
import com.pimentadesenvolvimento.exception.ResourceNotFoundException;
import com.pimentadesenvolvimento.exception.UserAlreadyExistsException;
import com.pimentadesenvolvimento.mapper.UserMapper;
import com.pimentadesenvolvimento.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final SecurityService securityService;
    private final ObjectProvider<UserEventPublisher> userEventPublisher;
    private final ObjectProvider<AuditLogService> auditLogService;
    private final PasswordPolicyService passwordPolicyService;
    private final RoleResolverService roleResolverService;


    public UserService(
            UserRepository userRepository,
            UserMapper userMapper,
            PasswordEncoder passwordEncoder,
            SecurityService securityService,
            ObjectProvider<UserEventPublisher> userEventPublisher,
            ObjectProvider<AuditLogService> auditLogService,
            PasswordPolicyService passwordPolicyService,
            RoleResolverService roleResolverService
    ) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.securityService = securityService;
        this.userEventPublisher = userEventPublisher;
        this.auditLogService = auditLogService;
        this.passwordPolicyService = passwordPolicyService;
        this.roleResolverService = roleResolverService;
    }

    @Transactional(readOnly = true)
    public UserResponse getFullUser(Long id) {
        User loggedUser = securityService.getLoggedUser();
        if (!securityService.isAdmin(loggedUser)) {
            throw new AccessDeniedException(Messages.UserService.ONLY_ADMINS_VIEW);
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(Messages.UserService.USER_NOT_FOUND_BY_ID, id)));

        return userMapper.toResponse(user);
    }

    /**
     * Convenience method used by controllers to retrieve the currently authenticated user.
     */
    public User getLoggedUser() {
        return securityService.getLoggedUser();
    }

    @Transactional
    public UserResponse updateFullUser(Long id, UserUpdateRequest request) {
        User loggedUser = securityService.getLoggedUser();
        if (!securityService.isAdmin(loggedUser)) {
            throw new AccessDeniedException(Messages.UserService.ONLY_ADMINS_EDIT);
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(Messages.UserService.USER_NOT_FOUND_BY_ID, id)));

        if (request.name() != null) {
            user.setName(request.name());
        }

        if (request.email() != null) {
            if (!request.email().equals(user.getEmail()) && userRepository.existsByEmailAndIdNot(request.email(), id)) {
                throw new UserAlreadyExistsException("Email already in use");
            }
            user.setEmail(request.email());
        }

        if (request.username() != null && !request.username().equals(user.getUsername())) {
            Optional<User> existing = userRepository.findByUsername(request.username());
            if (existing.isPresent() && !existing.get().getId().equals(id)) {
                throw new UserAlreadyExistsException(Messages.UserService.USERNAME_ALREADY_IN_USE);
            }
            user.setUsername(request.username());
        }

        if (request.password() != null && !request.password().isEmpty()) {
            passwordPolicyService.validate(request.password());
            user.setPassword(passwordEncoder.encode(request.password()));
        }

        if (request.roles() != null) {
            Set<Role> roles = roleResolverService.resolve(request.roles());
            user.setRoles(roles);
        }

        User updated = userRepository.save(user);

        return userMapper.toResponse(updated);
    }

    @Transactional
    public UserResponse create(UserCreateRequest request) {
        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new UserAlreadyExistsException("User already exists: " + request.username());
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("Email already in use");
        }

        User user = new User();
        user.setName(request.name());
        user.setUsername(request.username());
        user.setEmail(request.email());
        passwordPolicyService.validate(request.password());
        user.setPassword(passwordEncoder.encode(request.password()));

        Set<String> requestedRoles = Optional.ofNullable(request.roles()).orElse(Set.of());
        Set<Role> roles = roleResolverService.resolve(requestedRoles);
        user.setRoles(roles);

        user = userRepository.save(user);

        User publishedUser = user;
        userEventPublisher.ifAvailable(p -> p.publishUserCreated(publishedUser));

        User finalUser = user;
        auditLogService.ifAvailable(service -> {
            String principal = "system";
            try {
                principal = SecurityContextHolder.getContext().getAuthentication().getName();
            } catch (Exception e) {
                log.debug("Could not retrieve logged user from context", e);
            }
            service.log("USER_CREATED", principal, "id=" + finalUser.getId());
        });

        return userMapper.toResponse(user);
    }

    @Deprecated(since = "2.0.0", forRemoval = true)
    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAllWithRoles().stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> findAllPaginated(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(Messages.UserService.USER_NOT_FOUND_BY_ID, id)));

        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse update(Long id, UserUpdateRequest request) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(Messages.UserService.USER_NOT_FOUND_BY_ID, id)));

        if (request.name() != null) {
            existingUser.setName(request.name());
        }

        if (request.email() != null) {
            if (!request.email().equals(existingUser.getEmail()) && userRepository.existsByEmailAndIdNot(request.email(), id)) {
                throw new UserAlreadyExistsException("Email already in use");
            }
            existingUser.setEmail(request.email());
        }

        if (request.password() != null && !request.password().isEmpty()) {
            passwordPolicyService.validate(request.password());
            existingUser.setPassword(passwordEncoder.encode(request.password()));
        }

        if (request.roles() != null && !request.roles().isEmpty()) {
            Set<Role> roles = roleResolverService.resolve(request.roles());
            existingUser.setRoles(roles);
        }

        User updatedUser = userRepository.save(existingUser);

        auditLogService.ifAvailable(service -> {
            String principal = securityService.getLoggedUser().getUsername();
            service.log("USER_UPDATED", principal, "id=" + updatedUser.getId());
        });

        return userMapper.toResponse(updatedUser);
    }

    @Transactional
    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(Messages.UserService.USER_NOT_FOUND_BY_ID, id)));

        // Soft delete to keep audit trail and prevent accidental data loss.
        user.softDelete();
        userRepository.save(user);

        auditLogService.ifAvailable(service -> {
            String principal = securityService.getLoggedUser().getUsername();
            service.log("USER_DELETED", principal, "id=" + user.getId());
        });
    }
}
