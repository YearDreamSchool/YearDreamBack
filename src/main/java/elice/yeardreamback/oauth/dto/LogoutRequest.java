package elice.yeardreamback.oauth.dto;

/**
 * 로그아웃 요청 시 클라이언트로부터 전송되는 토큰 정보를 담는 DTO입니다.
 * 일반적으로 무효화할 Refresh Token을 담는 데 사용됩니다.
 */
public class LogoutRequest {
    /**
     * 클라이언트가 무효화하기 위해 전송하는 토큰 문자열입니다.
     */
    private String token;

    /**
     * 토큰 문자열을 반환합니다.
     * @return 무효화할 토큰
     */
    public String getToken() {
        return token;
    }

    /**
     * 토큰 문자열을 설정합니다.
     * @param token 무효화할 토큰
     */
    public void setToken(String token) {
        this.token = token;
    }
}