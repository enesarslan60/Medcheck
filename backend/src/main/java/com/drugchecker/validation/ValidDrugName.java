package com.drugchecker.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@NotBlank(message = "drug name must not be blank")
@Size(min = ValidationRules.DRUG_NAME_MIN, max = ValidationRules.DRUG_NAME_MAX,
        message = "drug name must be between {min} and {max} characters")
@Pattern(regexp = ValidationRules.DRUG_NAME_PATTERN,
        message = "drug name contains invalid characters")
@Constraint(validatedBy = {})
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDrugName {

    String message() default "invalid drug name";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
