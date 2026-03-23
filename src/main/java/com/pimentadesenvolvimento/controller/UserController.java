package com.pimentadesenvolvimento.controller;

import com.pimentadesenvolvimento.config.PageableValidator;
import com.pimentadesenvolvimento.domain.User;
import com.pimentadesenvolvimento.dto.UserCreateRequest;
import com.pimentadesenvolvimento.dto.UserPublicResponse;
import com.pimentadesenvolvimento.dto.UserResponse;
import com.pimentadesenvolvimento.dto.UserUpdateRequest;
import com.pimentadesenvolvimento.security.SecurityRoles;
import com.pimentadesenvolvimento.service.SecurityService;
import com.pimentadesenvolvimento.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Slf4j
@Validated
public class UserController {
    private final UserService userService;
    private final SecurityService securityService;
    private final PageableValidator pageableValidator;

    public UserController(UserService userService, SecurityService securityService, PageableValidator pageableValidator) {
        this.userService = userService;
        this.securityService = securityService;
        this.pageableValidator = pageableValidator;
    }

    @PreAuthorize(SecurityRoles.HAS_ADMIN)
    @GetMapping("/{id}/full")
    public ResponseEntity<UserResponse> getFullUser(@PathVariable Long id) {
        UserResponse user = userService.getFullUser(id);
        return ResponseEntity.ok(user);
    }

    @PreAuthorize(SecurityRoles.HAS_ADMIN)
    @PutMapping("/{id}/full")
    public ResponseEntity<UserResponse> updateFullUser(
            @PathVariable Long id,
            @RequestBody UserUpdateRequest request
    ) {
        UserResponse updated = userService.updateFullUser(id, request);
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize(SecurityRoles.HAS_ADMIN)
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
        UserResponse newUser = userService.create(request);
        return new ResponseEntity<>(newUser, HttpStatus.CREATED);
    }

    @PreAuthorize(SecurityRoles.HAS_ADMIN)
    @GetMapping
    public ResponseEntity<Page<UserResponse>> getAllUsers(@Valid Pageable pageable) {
        pageableValidator.validateUserPageable(pageable);
        Page<UserResponse> users = userService.findAllPaginated(pageable);
        return ResponseEntity.ok(users);
    }

    @PreAuthorize(SecurityRoles.HAS_ANY_USER_OR_ADMIN)
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        User loggedUser = userService.getLoggedUser();
        if (!securityService.isAdmin(loggedUser) && !loggedUser.getId().equals(id)) {
            throw new AccessDeniedException("Access denied: You can only view your own profile.");
        }
        UserResponse user = userService.findById(id);
        if (!securityService.isAdmin(loggedUser)) {
            return ResponseEntity.ok(new UserPublicResponse(user.userId(), user.name(), user.username()));
        }
        return ResponseEntity.ok(user);
    }

    @PreAuthorize(SecurityRoles.HAS_ANY_USER_OR_ADMIN)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @RequestBody UserUpdateRequest request
    ) {
        User loggedUser = userService.getLoggedUser();
        if (!securityService.isAdmin(loggedUser) && !loggedUser.getId().equals(id)) {
            throw new AccessDeniedException("Access denied: You can only update your own profile.");
        }
        UserResponse updatedUser = userService.update(id, request);
        if (!securityService.isAdmin(loggedUser)) {
            return ResponseEntity.ok(new UserPublicResponse(updatedUser.userId(), updatedUser.name(), updatedUser.username()));
        }
        return ResponseEntity.ok(updatedUser);
    }

    @PreAuthorize(SecurityRoles.HAS_ADMIN)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}