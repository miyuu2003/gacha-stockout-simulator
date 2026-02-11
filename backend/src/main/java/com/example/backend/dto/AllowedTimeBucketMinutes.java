package com.example.backend.dto;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * timeBucketMinutesフィールドの値が許可された値の
 * いずれかであることを検証するカスタムバリデーションアノテーション
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
//Validaterを指定
@Constraint(validatedBy = AllowedTimeBucketMinutesValidator.class)
public @interface AllowedTimeBucketMinutes {

    String message() default "timeBucketMinutes must be one of 5, 10, 15, 30, 60";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
