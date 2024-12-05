package com.software.security.zeroday.entity.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum FileType {
    TEMP("temp"),
    IMAGE("images"),
    VIDEO("videos");

    private final String folder;
}
