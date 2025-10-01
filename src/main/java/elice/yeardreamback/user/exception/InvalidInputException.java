package elice.yeardreamback.user.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 필수 입력 값이 누락되었거나 형식이 유효하지 않을 때 발생하는 예외.
 * HTTP 상태 코드 400 (Bad Request)를 반환하도록 지정합니다.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidInputException extends RuntimeException {

    public InvalidInputException(String fieldName, String reason) {
        super(fieldName + " 값이 유효하지 않습니다. 이유: " + reason);
    }

    public InvalidInputException(String message) {
        super(message);
    }
}