package ee.ng.events.event.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventDto {
    private String title;
    private LocalDateTime startAt;
    private Integer capacity;
}
