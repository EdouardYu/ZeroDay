package com.software.security.zeroday.dto.user;

import com.software.security.zeroday.entity.enumeration.Gender;
import com.software.security.zeroday.entity.enumeration.Nationality;
import lombok.Builder;
import lombok.Data;

import java.util.EnumSet;

@Data
@Builder
public class OptionsDTO {
    @Builder.Default
    private EnumSet<Gender> genders = EnumSet.noneOf(Gender.class);

    @Builder.Default
    private EnumSet<Nationality> nationalities = EnumSet.noneOf(Nationality.class);
}
