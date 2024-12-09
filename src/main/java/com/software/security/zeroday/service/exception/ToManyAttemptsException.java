package com.software.security.zeroday.service.exception;

public class ToManyAttemptsException extends RuntimeException {
    public ToManyAttemptsException(String msg) {
        super(msg);
    }
}
