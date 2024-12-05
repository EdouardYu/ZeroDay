package com.software.security.zeroday.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.software.security.zeroday.entity.enumeration.Gender;
import com.software.security.zeroday.entity.enumeration.Nationality;
import com.software.security.zeroday.entity.enumeration.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ProfileDTO {
    private String email;
    private String firstname;
    private String lastname;
    private String username;
    private LocalDate birthday;
    private Gender gender;
    private Nationality nationality;
    @JsonProperty("picture_url")
    private String pictureUrl;
    private String bio;
    private Role role;
}
