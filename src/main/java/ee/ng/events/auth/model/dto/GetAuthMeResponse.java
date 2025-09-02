package ee.ng.events.auth.model.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class GetAuthMeResponse {
    String email;
    List<String> roles;
}
