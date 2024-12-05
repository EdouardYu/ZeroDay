package com.software.security.zeroday.dto.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.software.security.zeroday.entity.enumeration.Gender;
import com.software.security.zeroday.entity.enumeration.Nationality;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class RegistrationDTO {
    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters long")
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!#$%&*+<=>?@^_-]).*$",
        message = "Password must contain at least one uppercase letter, one lowercase letter, " +
            "one number, and one special character (! # $ % & * + - < = > ? @ ^ _)"
    )
    private String password;

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

    @JsonCreator
    public RegistrationDTO(
        String email,
        String password,
        String firstname,
        String lastname,
        LocalDate birthday,
        Gender gender,
        Nationality nationality
    ) {
        this.email = email == null ? null : email.toLowerCase();
        this.password = password;
        this.firstname = firstname == null ? null : firstname.trim();
        this.lastname = lastname == null ? null : lastname.trim();
        this.birthday = birthday;
        this.gender = gender;
        this.nationality = nationality;
    }
}
