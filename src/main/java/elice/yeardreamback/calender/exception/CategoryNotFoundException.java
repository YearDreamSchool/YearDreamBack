package elice.yeardreamback.calender.exception;

/**
 * 카테고리를 찾을 수 없을 때 발생하는 예외
 */
public class CategoryNotFoundException extends RuntimeException {
    
    public CategoryNotFoundException(String message) {
        super(message);
    }
    
    public CategoryNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}