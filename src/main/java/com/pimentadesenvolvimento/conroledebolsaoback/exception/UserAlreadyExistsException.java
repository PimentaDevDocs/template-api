package com.pimentadesenvolvimento.conroledebolsaoback.exception;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}