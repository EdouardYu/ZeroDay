package com.software.security.zeroday.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.software.security.zeroday.entity.enumeration.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDTO {
    private String username;
    @JsonProperty("picture_url")
    private String pictureUrl;
    private Role role;
}
