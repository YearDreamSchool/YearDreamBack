package elice.yeardreamback.calender.exception;

/**
 * 이벤트를 찾을 수 없거나 접근 권한이 없을 때 발생하는 예외
 */
public class EventNotFoundException extends RuntimeException {
    
    public EventNotFoundException(String message) {
        super(message);
    }
    
    public EventNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}