package ee.ng.events.event.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostEventRequest {
    @NotNull
    @NotBlank
    private String name;
    @NotNull
    private LocalDateTime startsAt;
    @NotNull
    private Integer capacity;
}
