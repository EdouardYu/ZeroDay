package com.software.security.zeroday.service.exception;

public class SpelInjectionDetectedException extends RuntimeException {
    public SpelInjectionDetectedException(String msg) {
        super(msg);
    }
}
