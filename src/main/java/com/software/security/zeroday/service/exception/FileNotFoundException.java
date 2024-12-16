package com.software.security.zeroday.service.exception;

public class FileNotFoundException extends RuntimeException {
    public FileNotFoundException(String msg) {
        super(msg);
    }
}
