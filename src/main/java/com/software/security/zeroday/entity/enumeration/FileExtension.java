package com.software.security.zeroday.entity.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum FileExtension {
    JPEG("image/jpeg"),
    JPG("image/jpeg"),
    PNG("image/png"),
    GIF("image/gif"),
    WEBP("image/webp"),
    SVG("image/svg+xml"),
    HEIC("image/heic"),
    MP4("video/mp4"),
    WEBM("video/webm"),
    MOV("video/quicktime");

    private final String mimeType;
}
