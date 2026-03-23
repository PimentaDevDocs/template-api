package com.pimentadesenvolvimento.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class PasswordPolicyServiceTest {

    private final PasswordPolicyService passwordPolicyService = new PasswordPolicyService();

    @Test
    void testNullPassword() {
        assertThrows(IllegalArgumentException.class, () -> {
            passwordPolicyService.validate(null);
        });
    }

    @Test
    void testPasswordShorterThan10Chars() {
        assertThrows(IllegalArgumentException.class, () -> {
            passwordPolicyService.validate("Short1@");
        });
    }

    @Test
    void testPasswordLongerThan128Chars() {
        String longPassword = "A1@" + "a".repeat(126);
        assertThrows(IllegalArgumentException.class, () -> {
            passwordPolicyService.validate(longPassword);
        });
    }

    @Test
    void testPasswordWithoutUppercase() {
        assertThrows(IllegalArgumentException.class, () -> {
            passwordPolicyService.validate("password1@test");
        });
    }

    @Test
    void testPasswordWithoutLowercase() {
        assertThrows(IllegalArgumentException.class, () -> {
            passwordPolicyService.validate("PASSWORD1@TEST");
        });
    }

    @Test
    void testPasswordWithoutDigit() {
        assertThrows(IllegalArgumentException.class, () -> {
            passwordPolicyService.validate("PasswordTest@");
        });
    }

    @Test
    void testPasswordWithoutSpecialCharacter() {
        assertThrows(IllegalArgumentException.class, () -> {
            passwordPolicyService.validate("PasswordTest1");
        });
    }

    @Test
    void testPasswordContainsCommonPassword() {
        assertThrows(IllegalArgumentException.class, () -> {
            passwordPolicyService.validate("Password1@Test");
        });
    }

    @Test
    void testValidStrongPassword() {
        assertDoesNotThrow(() -> {
            passwordPolicyService.validate("MySecure1@Pass");
        });
    }
}
