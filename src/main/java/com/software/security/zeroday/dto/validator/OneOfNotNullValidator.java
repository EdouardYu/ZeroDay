package com.software.security.zeroday.dto.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Field;
import java.util.Arrays;

public class OneOfNotNullValidator implements ConstraintValidator<OneOfNotNull, Object> {
    private String[] fields;

    @Override
    public void initialize(OneOfNotNull constraintAnnotation) {
        this.fields = constraintAnnotation.fields();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) return false;

        return Arrays.stream(this.fields)
            .map(field -> {
                try {
                    Field declaredField = value.getClass().getDeclaredField(field);
                    declaredField.setAccessible(true);
                    return declaredField.get(value);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new RuntimeException("Invalid field specified for validation: " + field, e);
                }
            })
            .anyMatch(fieldValue -> fieldValue != null
                && !(fieldValue instanceof String && ((String) fieldValue).trim().isBlank()));
    }
}
