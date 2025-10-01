package elice.yeardreamback.user.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 이메일 등 고유해야 하는 필드가 이미 존재할 때 발생하는 예외.
 * HTTP 상태 코드 409 (Conflict)를 반환하도록 지정합니다.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String email) {
        super("이미 사용 중인 이메일 주소입니다: " + email);
    }
}