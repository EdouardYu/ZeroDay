package com.software.security.zeroday.service.exception;

public class AlreadyProcessedException extends RuntimeException {
    public AlreadyProcessedException(String msg) {
        super(msg);
    }
}
