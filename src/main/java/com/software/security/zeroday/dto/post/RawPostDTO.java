package com.software.security.zeroday.dto.post;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.software.security.zeroday.dto.enumeration.LastAction;
import com.software.security.zeroday.dto.user.UserDTO;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class RawPostDTO {
    private String content;
    @JsonProperty("file_url")
    private String fileUrl;
}

