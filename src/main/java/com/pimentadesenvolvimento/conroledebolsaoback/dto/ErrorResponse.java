package com.pimentadesenvolvimento.conroledebolsaoback.dto;

import java.time.LocalDateTime;

public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        String details
) {
}
