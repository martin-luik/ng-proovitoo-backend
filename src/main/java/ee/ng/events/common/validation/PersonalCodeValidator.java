package ee.ng.events.common.validation;

import ee.ng.events.common.util.PersonalCodeUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PersonalCodeValidator implements ConstraintValidator<PersonalCode, String> {

    private boolean required;

    @Override
    public void initialize(PersonalCode anno) {
        this.required = anno.required();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext ctx) {
        if (value == null || value.isBlank()) {
            return !required;
        }
        return PersonalCodeUtil.isValidPersonalCode(value);
    }
}
