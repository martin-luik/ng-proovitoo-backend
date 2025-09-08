package ee.ng.events.common.exception;

public class EventNotFoundException extends RuntimeException {
    public EventNotFoundException(Long id) { super("Event with id " + id + " was not found."); }
}


