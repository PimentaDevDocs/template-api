package com.pimentadesenvolvimento.dto;

/**
 * Public user response DTO for non-admin users.
 * Contains only non-sensitive profile information.
 */
public record UserPublicResponse(
        Long userId,
        String name,
        String username
) {
}
