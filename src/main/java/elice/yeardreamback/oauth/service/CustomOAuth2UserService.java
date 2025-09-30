package elice.yeardreamback.oauth.service;

import elice.yeardreamback.oauth.dto.*;
import elice.yeardreamback.user.dto.UserDTO;
import elice.yeardreamback.user.entity.User;
import elice.yeardreamback.user.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

/**
 * Spring Security OAuth2의 사용자 정보를 로드하는 커스텀 서비스입니다.
 * 소셜 로그인 사용자 정보를 받아와 내부 User 엔티티로 변환 및 저장하는 핵심 로직을 수행합니다.
 */
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    /**
     * 의존성 주입을 위한 생성자입니다.
     * @param userRepository 사용자 엔티티를 관리하는 리포지토리
     */
    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * OAuth2 리소스 서버로부터 사용자 정보를 로드한 후, 내부 로직을 통해 처리합니다.
     * @param userRequest OAuth2 요청 정보 (클라이언트 등록 정보, 액세스 토큰 등 포함)
     * @return CustomOAuth2User 객체 (SecurityContext에 저장됨)
     * @throws OAuth2AuthenticationException 인증 과정 중 오류 발생 시 예외 발생
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        // 1. 부모 클래스(DefaultOAuth2UserService)를 통해 OAuth2 사용자 정보를 기본적으로 로드합니다.
        OAuth2User oAuth2User = super.loadUser(userRequest);
        System.out.println(oAuth2User); // 💡 (디버깅용 로그)

        // 2. 현재 로그인 시도 중인 소셜 서비스(공급자)를 식별합니다.
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Response oAuth2Response;

        // 3. 공급자별로 응답(attributes)을 처리할 Response 객체를 생성합니다.
        if (registrationId.equals("naver")) {
            oAuth2Response = new NaverResponse(oAuth2User.getAttributes());
        } else if (registrationId.equals("google")) {
            oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());
        } else if (registrationId.equals("kakao")) {
            oAuth2Response = new KakaoResponse(oAuth2User.getAttributes());
        } else {
            // 지원하지 않는 공급자일 경우
            return null;
        }

        // 4. 고유한 사용자 이름(username)을 생성합니다. (예: "naver 123456789")
        String username = oAuth2Response.getProvider() + " " + oAuth2Response.getProviderId();

        // 5. 데이터베이스에 사용자가 존재하는지 확인하고, 처리합니다.
        User user = userRepository.findByUsername(username)
                .map(existingUser -> {
                    // 5-1. 이미 존재하는 사용자라면, 이름과 이메일을 업데이트합니다.
                    existingUser.setName(oAuth2Response.getName());
                    existingUser.setEmail(oAuth2Response.getEmail());
                    return userRepository.save(existingUser);
                })
                .orElseGet(() -> {
                    // 5-2. 새로운 사용자라면, User 엔티티를 생성하고 저장합니다.
                    User newUser = new User();
                    newUser.setName(oAuth2Response.getName());
                    newUser.setUsername(username);
                    newUser.setEmail(oAuth2Response.getEmail());
                    newUser.setRole("USER"); // 기본 역할 설정
                    return userRepository.save(newUser);
                });

        // 6. DB에서 처리된 User 정보를 바탕으로 CustomOAuth2User에 전달할 DTO를 생성합니다.
        UserDTO userDTO = UserDTO.builder()
                .username(user.getUsername())
                .name(user.getName())
                .role(user.getRole())
                .build();

        // 7. CustomOAuth2User 객체를 반환하여 Security Context에 저장합니다.
        return new CustomOAuth2User(userDTO);
    }
}