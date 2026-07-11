package com.drugchecker.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@NotEmpty(message = "drugNames must not be empty")
@Size(min = ValidationRules.DRUG_LIST_MIN, max = ValidationRules.DRUG_LIST_MAX,
        message = "drugNames must contain between {min} and {max} entries")
@Constraint(validatedBy = {})
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDrugNameList {

    String message() default "invalid drug list";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
