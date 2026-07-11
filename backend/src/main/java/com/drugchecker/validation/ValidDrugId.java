package com.drugchecker.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@NotNull(message = "drug id must not be null")
@Min(value = 1, message = "drug id must be >= 1")
@Constraint(validatedBy = {})
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDrugId {

    String message() default "invalid drug id";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
