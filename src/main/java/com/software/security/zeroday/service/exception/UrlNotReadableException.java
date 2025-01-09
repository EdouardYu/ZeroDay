package com.software.security.zeroday.service.exception;

public class UrlNotReadableException extends RuntimeException {
    public UrlNotReadableException(String msg) {
        super(msg);
    }
}
