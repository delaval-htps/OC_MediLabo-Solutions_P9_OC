package com.medilabosolutions.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = DateFomatValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD,ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomDateFormat {
    String message() default "Invalid date format";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
