package com.software.security.zeroday.dto.post;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.software.security.zeroday.dto.user.UserDTO;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ParentDTO {
    private UserDTO user;
    private String content;
    @JsonProperty("file_url")
    private String fileUrl;
}

