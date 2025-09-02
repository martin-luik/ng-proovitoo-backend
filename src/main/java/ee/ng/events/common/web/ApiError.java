package ee.ng.events.common.web;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiError {
    private ErrorCode code;
    private String message;
}
