package com.software.security.zeroday.dto.post;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.software.security.zeroday.dto.validator.OneOfNotNull;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@OneOfNotNull(fields = {"fileId", "content"}, message = "Either fileId or content must be provided")
public class PostModificationDTO {
    @Size(max = 3000, message = "Content must be at most 3000 characters long")
    private String content;

    @JsonProperty("file_id")
    private Long fileId;
}
