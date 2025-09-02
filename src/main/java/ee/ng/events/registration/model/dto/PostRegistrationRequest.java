package ee.ng.events.registration.model.dto;

import ee.ng.events.common.validation.PersonalCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PostRegistrationRequest {
    @NotNull
    @NotBlank
    private String firstName;
    @NotNull
    @NotBlank
    private String lastName;
    @NotNull
    @PersonalCode
    private String personalCode;
}
