package elice.yeardreamback.user.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 데이터베이스에서 요청된 사용자(User)를 찾을 수 없을 때 발생하는 커스텀 런타임 예외입니다.
 * 이 예외는 보통 HTTP 404 Not Found 응답을 반환하는 데 사용됩니다.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundException extends RuntimeException {

    /**
     * 찾을 수 없었던 username 정보를 포함하는 상세 메시지를 사용하여 예외를 생성합니다.
     * * @param username 찾으려고 시도했지만 존재하지 않는 사용자 이름 (식별자)
     */
    public UserNotFoundException(String username) {
        super("User not found with username: " + username);
    }
}