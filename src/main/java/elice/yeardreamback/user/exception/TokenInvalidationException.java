package elice.yeardreamback.user.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 토큰 무효화(로그아웃) 처리 중 문제가 발생했을 때 발생하는 예외.
 * HTTP 상태 코드 400 (Bad Request) 또는 500 (Internal Server Error)을 고려할 수 있습니다.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class TokenInvalidationException extends RuntimeException {

    public TokenInvalidationException(String token, String reason) {
        super("토큰 무효화에 실패했습니다. (토큰: " + token + ", 이유: " + reason + ")");
    }

    public TokenInvalidationException(String reason) {
        super("토큰 무효화에 실패했습니다. 이유: " + reason);
    }
}