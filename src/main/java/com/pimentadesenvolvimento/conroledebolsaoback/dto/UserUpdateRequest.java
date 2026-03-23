package com.pimentadesenvolvimento.conroledebolsaoback.dto;

import com.pimentadesenvolvimento.conroledebolsaoback.config.Messages;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record UserUpdateRequest(
        @Size(min = 3, max = 50, message = Messages.Validation.USERNAME_SIZE)
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = Messages.Validation.USERNAME_PATTERN)
        String username,

        @Size(min = 6, message = Messages.Validation.PASSWORD_MIN)
        String password,

        @Email(message = Messages.Validation.EMAIL_INVALID)
        String email,

        String name,

        Set<String> roles
) {
}
