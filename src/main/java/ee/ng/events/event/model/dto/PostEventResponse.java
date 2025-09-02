package ee.ng.events.event.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostEventResponse {
    private Long id;
    private String name;
    private LocalDateTime startsAt;
    private Integer capacity;
}
