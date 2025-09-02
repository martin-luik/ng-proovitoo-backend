package ee.ng.events.event.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GetEventResponse {
    private Long id;
    private String name;
    private LocalDateTime startsAt;
    private Integer capacity;
    private Long registrationsCount;
    private Integer availableSeats;
}
