package com.pimentadesenvolvimento.conroledebolsaoback.dto;

import java.time.LocalDateTime;
import java.util.Set;

public record UserResponse(
        Long userId,
        String name,
        String username,
        String email,
        Set<RoleDTO> roles,
        PersonDTO person,
        LocalDateTime createdAt,
        String createdBy
) {
}
