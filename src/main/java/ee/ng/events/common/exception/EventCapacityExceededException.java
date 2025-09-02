package ee.ng.events.common.exception;

public class EventCapacityExceededException extends RuntimeException {
    public EventCapacityExceededException(Long id) { super("Event is full: " + id); }
}
