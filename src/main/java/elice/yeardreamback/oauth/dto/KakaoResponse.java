package elice.yeardreamback.oauth.dto;

import java.util.Map;

/**
 * Kakao OAuth2 인증 후 리소스 서버에서 반환되는 사용자 속성(attributes)을 처리하여
 * 표준화된 OAuth2Response 인터페이스 형태로 데이터를 제공하는 클래스입니다.
 * Kakao의 사용자 정보는 중첩된 Map 형태로 제공됩니다.
 */
public class KakaoResponse implements OAuth2Response {

    // Kakao 리소스 서버에서 받은 원본 사용자 속성 맵
    private final Map<String, Object> attributes;

    /**
     * KakaoResponse 객체를 초기화합니다.
     * @param attributes Kakao 리소스 서버에서 받은 사용자 속성(Key-Value) 맵
     */
    public KakaoResponse(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    /**
     * OAuth2 공급자 이름을 반환합니다.
     * @return "kakao"
     */
    @Override
    public String getProvider() {
        return "kakao";
    }

    /**
     * Kakao에서 제공하는 사용자의 고유 식별자(id)를 반환합니다.
     * 이 'id'는 최상위 레벨에 위치합니다.
     * @return Kakao 고유 ID (id 필드)
     */
    @Override
    public String getProviderId() {
        return attributes.get("id").toString();
    }

    /**
     * Kakao 계정의 닉네임을 반환합니다.
     * 닉네임은 'kakao_account' > 'profile' 맵 내부에 중첩되어 있습니다.
     * @return 사용자의 닉네임 (nickname 필드)
     */
    @Override
    public String getName() {
        // 'kakao_account' 맵을 추출
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        // 'profile' 맵을 추출
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        return profile.get("nickname").toString();
    }

    /**
     * Kakao 계정의 이메일 주소를 반환합니다.
     * 이메일은 'kakao_account' 맵 내부에 위치하며, 동의 여부에 따라 null일 수 있습니다.
     * @return 이메일 주소 (email 필드). 동의하지 않은 경우 빈 문자열("") 반환
     */
    @Override
    public String getEmail() {
        // 'kakao_account' 맵을 추출
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");

        // 이메일 필드가 존재하는지 확인 후 반환합니다.
        return kakaoAccount.get("email") != null ? kakaoAccount.get("email").toString() : "";
    }
}