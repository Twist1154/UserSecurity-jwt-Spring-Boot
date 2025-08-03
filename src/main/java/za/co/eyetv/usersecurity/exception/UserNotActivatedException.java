package za.co.eyetv.usersecurity.exception;



public class UserNotActivatedException extends RuntimeException {
    public UserNotActivatedException(String message) {
        super(message);
    }

    public UserNotActivatedException(String message, Throwable cause) {
        super(message, cause);
    }
}
