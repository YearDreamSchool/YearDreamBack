package elice.yeardreamback.oauth.dto;

import java.util.Map;

/**
 * Naver OAuth2 인증 후 리소스 서버에서 반환되는 사용자 속성(attributes)을 처리하여
 * 표준화된 OAuth2Response 인터페이스 형태로 데이터를 제공하는 클래스입니다.
 * Naver의 실제 사용자 정보는 최상위 맵의 "response" 키 아래에 중첩되어 있습니다.
 */
public class NaverResponse implements OAuth2Response {

    // Naver 응답에서 "response" 키 아래에 있는 실제 사용자 속성 맵
    private final Map<String, Object> attribute;

    /**
     * NaverResponse 객체를 초기화합니다.
     * Naver 응답의 최상위 맵에서 "response" 키에 해당하는 맵을 추출하여 저장합니다.
     * @param attribute Naver 리소스 서버에서 받은 최상위 사용자 속성 맵
     */
    public NaverResponse(Map<String, Object> attribute) {
        // Naver 응답은 "response" 키 아래에 실제 정보가 담겨 있습니다.
        this.attribute = (Map<String, Object>) attribute.get("response");

        // 💡 디버깅용 로그는 운영 코드에서는 제거하는 것이 좋습니다.
        System.out.println("NAVER ATTRIBUTES: " + attribute);
    }

    /**
     * OAuth2 공급자 이름을 반환합니다.
     * @return "naver"
     */
    @Override
    public String getProvider() {
        return "naver";
    }

    /**
     * Naver에서 제공하는 사용자의 고유 식별자(id)를 반환합니다.
     * @return Naver 고유 ID (id 필드)
     */
    @Override
    public String getProviderId() {
        return attribute.get("id").toString();
    }

    /**
     * Naver 계정에 설정된 사용자 이름을 반환합니다.
     * @return 이름 (name 필드)
     */
    @Override
    public String getName() {
        return attribute.get("name").toString();
    }

    /**
     * Naver 계정의 이메일 주소를 반환합니다.
     * @return 이메일 주소 (email 필드)
     */
    @Override
    public String getEmail() {
        return attribute.get("email").toString();
    }
}