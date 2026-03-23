package com.pimentadesenvolvimento.conroledebolsaoback.exception;

public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}