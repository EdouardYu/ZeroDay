package com.software.security.zeroday.dto.file;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.MediaType;

@Data
@Builder
public class FileDTO {
    private MediaType mimeType;
    private byte[] content;
}