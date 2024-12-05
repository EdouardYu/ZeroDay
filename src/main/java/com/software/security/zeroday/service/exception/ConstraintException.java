package com.software.security.zeroday.service.exception;

public class ConstraintException extends RuntimeException {
    public ConstraintException(String msg) {
        super(msg);
    }
}
