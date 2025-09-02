package ee.ng.events.common.web;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(staticName = "of")
public class ApiError {
    ErrorCode code;
    String message;
}
