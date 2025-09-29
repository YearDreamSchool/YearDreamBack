package elice.yeardreamback.calender.exception;

/**
 * 이벤트 공유 작업이 실패할 때 발생하는 예외
 */
public class EventSharingException extends RuntimeException {
    
    public EventSharingException(String message) {
        super(message);
    }
    
    public EventSharingException(String message, Throwable cause) {
        super(message, cause);
    }
}