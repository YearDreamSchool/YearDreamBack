package elice.yeardreamback.user.exception;

/**
 * 사용자가 인증되지 않았거나 (로그인하지 않았거나),
 * 제공된 인증 정보(예: JWT 토큰)가 유효하지 않을 때 발생하는 커스텀 런타임 예외입니다.
 * * 이 예외는 보통 HTTP 401 Unauthorized 응답을 반환하는 데 사용됩니다.
 */
public class UserNotAuthenticatedException extends RuntimeException {

    /**
     * 지정된 상세 메시지를 사용하여 새로운 UserNotAuthenticatedException을 생성합니다.
     * * @param message 예외에 대한 상세 설명 (클라이언트에게 반환될 수 있음)
     */
    public UserNotAuthenticatedException(String message) {
        super(message);
    }
}