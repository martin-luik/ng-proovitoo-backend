package ee.ng.events.event.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GetEventResponse {
    private Long id;
    private String title;
    private LocalDateTime startAt;
    private Integer capacity;
    private Long registrationsCount;
    private Integer availableSeats;
}
