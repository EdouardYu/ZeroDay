package com.software.security.zeroday.service.exception;

public class AlreadyUsedException extends RuntimeException {
    public AlreadyUsedException(String msg) {
        super(msg);
    }
}
