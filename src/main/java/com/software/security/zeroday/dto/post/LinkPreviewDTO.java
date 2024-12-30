package com.software.security.zeroday.dto.post;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LinkPreviewDTO {
    private String title;
    private String content;
}

