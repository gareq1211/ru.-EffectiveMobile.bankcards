package ru.effectivemobile.bankcards.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UniquePanValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UniquePan {
    String message() default "Card with this PAN already exists";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}