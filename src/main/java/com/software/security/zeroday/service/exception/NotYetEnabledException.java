package com.software.security.zeroday.service.exception;

public class NotYetEnabledException extends RuntimeException {
    public NotYetEnabledException(String msg) {
        super(msg);
    }
}
