package ee.ng.events.common.web;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    NOT_FOUND("event.not.found"),
    EVENT_FULL("event.full"),
    ALREADY_REGISTERED("already.registered"),
    VALIDATION_ERROR("validation.error"),
    CONSTRAINT_VIOLATION("constraint.violation"),
    CONFLICT("conflict"),
    INTERNAL_ERROR("internal.error");

    private final String code;

    @JsonValue
    public String json() {
        return code;
    }
}
