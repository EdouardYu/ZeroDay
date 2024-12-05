package com.software.security.zeroday.service.exception;

public class ValidationCodeException extends RuntimeException {
    public ValidationCodeException(String msg) {
        super(msg);
    }
}
