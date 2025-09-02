package ee.ng.events.auth.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PostAuthRequest {
    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;
}
