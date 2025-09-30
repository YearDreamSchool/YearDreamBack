package elice.yeardreamback.oauth.dto;

import elice.yeardreamback.user.dto.UserDTO;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Spring Security의 OAuth2 인증 흐름에서 최종적으로 SecurityContext에 저장되는 사용자 객체입니다.
 * OAuth2User 인터페이스를 구현하여 사용자 정보(UserDTO)와 권한 정보를 통합합니다.
 */
public class CustomOAuth2User implements OAuth2User {

    private final UserDTO userDTO;

    /**
     * UserDTO를 기반으로 CustomOAuth2User 객체를 생성합니다.
     * @param userDTO 데이터베이스에서 조회하거나 새로 생성된 사용자 정보 DTO
     */
    public CustomOAuth2User(UserDTO userDTO) {
        this.userDTO = userDTO;
    }

    /**
     * OAuth2 공급자로부터 받은 원본 속성(attributes) 맵을 반환합니다.
     * 현재 구현에서는 사용하지 않으므로 null을 반환합니다.
     * @return null
     */
    @Override
    public Map<String, Object> getAttributes() {
        return null;
    }

    /**
     * 사용자에게 부여된 권한 목록을 반환합니다.
     * UserDTO의 role 필드를 사용하여 Spring Security가 요구하는 "ROLE_" 접두사가 붙은 권한 객체를 생성합니다.
     * @return 부여된 권한(GrantedAuthority) 컬렉션
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        Collection<GrantedAuthority> collection = new ArrayList<>();

        collection.add(new SimpleGrantedAuthority("ROLE_" + userDTO.getRole()));
        return collection;
    }

    /**
     * 리소스 서버가 식별하는 사용자 이름을 반환합니다.
     * 여기서는 DTO의 이름을 반환합니다.
     * @return 사용자의 이름
     */
    @Override
    public String getName() {
        return userDTO.getName();
    }

    /**
     * 사용자 식별자(username)를 반환합니다.
     * JWT 토큰 생성 등에 사용될 수 있습니다.
     * @return 사용자의 고유 ID (username)
     */
    public String getUsername() {
        return userDTO.getUsername();
    }

    /**
     * 사용자의 역할(Role) 정보를 반환합니다.
     * @return 사용자의 역할 문자열 (예: "USER", "ADMIN", "COACH")
     */
    public String getRole() {
        return userDTO.getRole();
    }
}
