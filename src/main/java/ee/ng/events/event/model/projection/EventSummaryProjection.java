package ee.ng.events.event.model.projection;

import java.time.LocalDateTime;

public interface EventSummaryProjection {
    Long getId();

    String getName();

    LocalDateTime getStartsAt();

    int getCapacity();

    long getRegistrationsCount();

    default int getAvailableSeats() {
        return getCapacity() - (int) getRegistrationsCount();
    }
}
