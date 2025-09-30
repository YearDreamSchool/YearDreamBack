package elice.yeardreamback.oauth.dto;

import java.util.Map;

/**
 * Google OAuth2 인증 후 리소스 서버에서 반환되는 사용자 속성(attributes)을 처리하여
 * 표준화된 OAuth2Response 인터페이스 형태로 데이터를 제공하는 클래스입니다.
 */
public class GoogleResponse implements OAuth2Response{

    private final Map<String, Object> attribute;

    /**
     * GoogleResponse 객체를 초기화합니다.
     * @param attribute Google리소스 서버에서 받은 사용자 속성 (Key-Value) 맵
     */
    public GoogleResponse(Map<String, Object> attribute) {
        this.attribute = attribute;
    }

    /**
     * OAuth2 공급자 이름을 반환합니다.
     * @return "google"
     */
    @Override
    public String getProvider() {
        return "google";
    }

    /**
     * Google에서 제공하는 사용자의 고유 식별자(sub)를 반환합니다.
     * @return Google 고유 ID (sub 필드)
     */
    @Override
    public String getProviderId() {
        // 'sub' 필드는 Google의 사용자 고유 ID입니다.
        return attribute.get("sub").toString();
    }

    /**
     * Google 계정의 이메일 주소를 반환합니다.
     * @return 이메일 주소 (email 필드)
     */
    @Override
    public String getEmail() {
        return attribute.get("email").toString();
    }

    /**
     * Google 계정에 설정된 사용자 이름을 반환합니다.
     * @return 이름 (name 필드)
     */
    @Override
    public String getName() {
        return attribute.get("name").toString();
    }
}
