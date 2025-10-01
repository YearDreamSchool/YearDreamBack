package elice.yeardreamback.user.exception;

public class UnauthorizedUserAccessException extends RuntimeException {
    public UnauthorizedUserAccessException(String message) {
        super(message);
    }
}
