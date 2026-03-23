package com.pimentadesenvolvimento.conroledebolsaoback.dto;

import java.time.LocalDateTime;

public record AuditLogDTO(
        Long id,
        String action,
        String principal,
        String details,
        LocalDateTime createdAt
) {
}
