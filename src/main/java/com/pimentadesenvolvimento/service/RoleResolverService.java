package com.pimentadesenvolvimento.service;

import com.pimentadesenvolvimento.domain.Role;
import com.pimentadesenvolvimento.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for resolving and validating role assignments.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RoleResolverService {

    private static final String DEFAULT_ROLE_NAME = "ROLE_USER";

    private final RoleRepository roleRepository;

    /**
     * Resolves role names to Role entities with validation.
     *
     * @param roleNames set of role names to resolve
     * @return set of resolved Role entities
     * @throws IllegalArgumentException if any role doesn't exist or validation fails
     */
    public Set<Role> resolve(Set<String> roleNames) {
        if (roleNames == null || roleNames.isEmpty()) {
            // Default role if none provided
            return roleRepository.findByName(DEFAULT_ROLE_NAME)
                    .map(Set::of)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Default role '" + DEFAULT_ROLE_NAME + "' not found in database"));
        }

        Set<String> normalized = roleNames.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toUpperCase)
                .collect(Collectors.toSet());

        if (!normalized.contains(DEFAULT_ROLE_NAME)) {
            normalized = new HashSet<>(normalized);
            normalized.add(DEFAULT_ROLE_NAME);
        }

        Set<Role> roles = new HashSet<>();
        StringBuilder invalidRoles = new StringBuilder();

        for (String roleName : normalized) {
            Role role = roleRepository.findByName(roleName)
                    .orElse(null);

            if (role == null) {
                // Log invalid roles but don't create them automatically
                if (invalidRoles.length() > 0) {
                    invalidRoles.append(", ");
                }
                invalidRoles.append(roleName);
                log.warn("Role '{}' requested but not found in database. Skipping.", roleName);
            } else {
                roles.add(role);
            }
        }

        if (invalidRoles.length() > 0) {
            throw new IllegalArgumentException(
                    "One or more requested roles do not exist: " + invalidRoles);
        }

        if (roles.isEmpty()) {
            throw new IllegalArgumentException("No valid roles could be assigned");
        }

        return roles;
    }
}
