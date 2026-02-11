package com.example.backend.dto;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Set;
/**
 * AllowedTimeBucketMinutesアノテーションのバリデータ
 */
public class AllowedTimeBucketMinutesValidator implements ConstraintValidator<AllowedTimeBucketMinutes, Integer> {
    private static final Set<Integer> ALLOWED_VALUES = Set.of(5, 10, 15, 30, 60);

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return ALLOWED_VALUES.contains(value);
    }
}
