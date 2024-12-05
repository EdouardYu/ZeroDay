package com.software.security.zeroday.dto.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.software.security.zeroday.entity.enumeration.Gender;
import com.software.security.zeroday.entity.enumeration.Nationality;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class ProfileModificationDTO {
    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Username cannot be empty")
    @Size(min = 3, max = 129, message = "Username must be between 3 and 129 characters long")
    @Pattern(regexp = "^[\\p{L}0-9 _-]+$", message = "Username can only contain letters, numbers, spaces, underscores, and hyphens")
    private String username;

    @NotBlank(message = "Firstname cannot be empty")
    @Size(max = 64, message = "Firstname must at most 64 characters long")
    @Pattern(regexp = "^[\\p{L} '-]+$", message = "Firstname can only contain letters, spaces, hyphens, and apostrophes")
    private String firstname;

    @NotBlank(message = "Last name cannot be empty")
    @Size(max = 64, message = "Lastname must be at most 64 characters long")
    @Pattern(regexp = "^[\\p{L} '-]+$", message = "Lastname can only contain letters, spaces, hyphens, and apostrophes")
    private String lastname;

    @NotNull(message = "Birthday cannot be null")
    @Past(message = "Birthday cannot be in the future")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;

    @NotNull(message = "Gender cannot be null")
    private Gender gender;

    @NotNull(message = "Nationality cannot be null")
    private Nationality nationality;

    @JsonProperty("picture_id")
    private Long pictureId;

    private String bio;

    @JsonCreator
    public ProfileModificationDTO(
        String email,
        String username,
        String firstname,
        String lastname,
        LocalDate birthday,
        Gender gender,
        Nationality nationality,
        Long pictureId,
        String bio
    ) {
        this.email = email == null ? null : email.toLowerCase();
        this.username = username == null ? null : username.trim();
        this.firstname = firstname == null ? null : firstname.trim();
        this.lastname = lastname == null ? null : lastname.trim();
        this.birthday = birthday;
        this.gender = gender;
        this.nationality = nationality;
        this.pictureId = pictureId;
        this.bio = bio == null ? null : bio.trim();
    }
}
