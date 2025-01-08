package com.software.security.zeroday.entity.object;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EvaluatedPostContent {
    private final String content;
    private final String parentContent;
}