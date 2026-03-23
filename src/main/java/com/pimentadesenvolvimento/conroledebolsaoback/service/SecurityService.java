package com.pimentadesenvolvimento.conroledebolsaoback.service;

import com.pimentadesenvolvimento.conroledebolsaoback.config.Messages;
import com.pimentadesenvolvimento.conroledebolsaoback.domain.User;
import com.pimentadesenvolvimento.conroledebolsaoback.exception.BusinessException;
import com.pimentadesenvolvimento.conroledebolsaoback.repository.UserRepository;
import com.pimentadesenvolvimento.conroledebolsaoback.security.SecurityRoles;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SecurityService {
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public User getLoggedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException(Messages.Security.USER_NOT_AUTHENTICATED);
        }
        Object principal = authentication.getPrincipal();
        String username;
        if (principal instanceof UserDetails userDetails) {
            username = userDetails.getUsername();
        } else if (principal instanceof String principalString) {
            username = principalString;
        } else {
            throw new IllegalStateException("Unexpected principal: " + principal.getClass().getName());
        }
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(
                        String.format(Messages.Security.LOGGED_USER_NOT_FOUND, username)
                ));
    }

    public boolean isAdmin(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName().equals(SecurityRoles.NAME_ROLE_ADMIN));
    }

    public boolean isSupervisor(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName().equals(SecurityRoles.NAME_ROLE_SUPERVISOR));
    }

    public Long getLoggedUserId() {
        return getLoggedUser().getId();
    }
}