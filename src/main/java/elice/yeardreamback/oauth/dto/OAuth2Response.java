package elice.yeardreamback.oauth.dto;

/**
 * 다양한 OAuth2 리소스 서버(Google, Kakao, Naver 등)로부터 받은
 * 사용자 속성(Attributes)을 표준화하여 접근하기 위한 인터페이스입니다.
 * 이 인터페이스를 구현하는 클래스는 각 공급자별 응답 구조를 처리하여 통일된 데이터를 제공해야 합니다.
 */
public interface OAuth2Response {

    /**
     * OAuth2 인증을 제공한 서비스 공급자의 이름을 반환합니다.
     * @return 공급자 이름 (예: "google", "kakao", "naver")
     */
    String getProvider();

    /**
     * 해당 공급자 내에서 사용자를 식별하는 고유 ID를 반환합니다.
     * 이 값은 사용자 식별자(username)로 사용될 수 있습니다.
     * @return 공급자 고유 ID
     */
    String getProviderId();

    /**
     * 사용자의 이메일 주소를 반환합니다.
     * @return 이메일 주소
     */
    String getEmail();

    /**
     * 사용자의 이름 또는 닉네임을 반환합니다.
     * @return 이름
     */
    String getName();
}