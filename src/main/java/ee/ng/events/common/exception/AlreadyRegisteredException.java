package ee.ng.events.common.exception;

public class AlreadyRegisteredException extends RuntimeException {
    public AlreadyRegisteredException(Long id) {
        super("Already registered for event: " + id);
    }
}
