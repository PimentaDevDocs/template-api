package com.pimentadesenvolvimento.service;

import com.pimentadesenvolvimento.config.Messages;
import org.springframework.stereotype.Service;

/**
 * Service for password validation and policy enforcement.
 */
@Service
public class PasswordPolicyService {

    /**
     * Validates password strength according to security policy.
     *
     * @param rawPassword the password to validate
     * @throws IllegalArgumentException if password does not meet requirements
     */
    public void validate(String rawPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException(Messages.Validation.PASSWORD_MISSING);
        }
        String pwd = rawPassword.trim();
        if (pwd.length() < 10) {
            throw new IllegalArgumentException(Messages.Validation.PASSWORD_MIN_STRONG);
        }
        if (pwd.length() > 128) {
            throw new IllegalArgumentException(Messages.Validation.PASSWORD_MAX_STRONG);
        }
        boolean hasUpper = pwd.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = pwd.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = pwd.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = pwd.chars().anyMatch(ch -> !Character.isLetterOrDigit(ch));
        if (!(hasUpper && hasLower && hasDigit && hasSpecial)) {
            throw new IllegalArgumentException(Messages.Validation.PASSWORD_COMPLEXITY);
        }
        String lower = pwd.toLowerCase();
        if (lower.contains("password") || lower.contains("senha") || lower.contains("123456") || lower.contains("qwerty")) {
            throw new IllegalArgumentException(Messages.Validation.PASSWORD_TOO_COMMON);
        }
    }
}
