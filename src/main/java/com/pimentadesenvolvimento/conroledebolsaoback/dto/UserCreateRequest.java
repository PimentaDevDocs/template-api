package com.pimentadesenvolvimento.conroledebolsaoback.dto;

import com.pimentadesenvolvimento.conroledebolsaoback.config.Messages;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record UserCreateRequest(
        @NotBlank(message = Messages.Validation.USERNAME_REQUIRED)
        @Size(min = 3, max = 50, message = Messages.Validation.USERNAME_SIZE)
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = Messages.Validation.USERNAME_PATTERN)
        String username,

        @NotBlank(message = Messages.Validation.PASSWORD_REQUIRED)
        @Size(min = 6, message = Messages.Validation.PASSWORD_MIN)
        String password,

        @NotBlank(message = Messages.Validation.EMAIL_REQUIRED)
        @Email(message = Messages.Validation.EMAIL_INVALID)
        String email,

        @NotBlank(message = "Name is required")
        String name,

        Set<String> roles
) {
}
