package com.software.security.zeroday.service.exception;

public class BadPasswordException extends RuntimeException {
    public BadPasswordException(String msg) {
        super(msg);
    }
}
