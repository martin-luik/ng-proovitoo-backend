package ee.ng.events.common.exception;

public class AlreadyRegisteredException extends RuntimeException {
    public AlreadyRegisteredException(Long id) {
        super("Registration already exists for event with id " + id);
    }
}
