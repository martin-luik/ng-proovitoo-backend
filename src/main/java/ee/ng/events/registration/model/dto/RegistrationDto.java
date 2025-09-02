package ee.ng.events.registration.model.dto;

import lombok.Data;

@Data
public class RegistrationDto {
    private Long eventId;
    private String firstName;
    private String lastName;
    private String personalCode;
}
