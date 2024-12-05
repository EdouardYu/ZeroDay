package com.software.security.zeroday.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorEntity {
    private int status;
    private String message;
}

