package ee.ng.events.event.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventDto {
    private Long id;
    private String name;
    private LocalDateTime startsAt;
    private Integer capacity;
}
