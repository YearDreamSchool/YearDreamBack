package elice.yeardreamback.calender.exception;

/**
 * 이벤트 시간이 유효하지 않을 때 발생하는 예외
 * (예: 시작 시간이 종료 시간보다 늦은 경우)
 */
public class InvalidEventTimeException extends RuntimeException {
    
    public InvalidEventTimeException(String message) {
        super(message);
    }
    
    public InvalidEventTimeException(String message, Throwable cause) {
        super(message, cause);
    }
}