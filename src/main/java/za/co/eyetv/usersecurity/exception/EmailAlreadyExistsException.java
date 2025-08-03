package za.co.eyetv.usersecurity.exception;


/**
 * Exception thrown when a user tries to register with an email that already exists in the system.
 * This exception is a subclass of {@link RuntimeException}.
 *
 * @author Rethabile Ntsekhe
 * @date 09-04-2025
 */
public class EmailAlreadyExistsException extends RuntimeException {
    private final String email;

    public EmailAlreadyExistsException(String email) {
        super("Email '" + email + "' is already registered.");
        this.email = email;
    }

    public EmailAlreadyExistsException(String email, Throwable cause) {
        super("Email '" + email + "' is already registered.", cause);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}