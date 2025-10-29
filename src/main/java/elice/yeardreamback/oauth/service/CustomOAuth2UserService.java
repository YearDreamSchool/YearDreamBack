package elice.yeardreamback.oauth.service;

import elice.yeardreamback.oauth.dto.*;
import elice.yeardreamback.user.dto.UserDTO;
import elice.yeardreamback.user.entity.User;
import elice.yeardreamback.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

/**
 * Spring Security OAuth2의 사용자 정보를 로드하는 커스텀 서비스입니다.
 * 소셜 로그인 사용자 정보를 받아와 내부 User 엔티티로 변환 및 저장하는 핵심 로직을 수행합니다.
 */
@Slf4j
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

        OAuth2User oAuth2User = super.loadUser(userRequest);
        log.debug("OAuth2 raw attributes: {}", oAuth2User.getAttributes());

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Response oAuth2Response = switch (registrationId) {
            case "naver" -> new NaverResponse(oAuth2User.getAttributes());
            case "google" -> new GoogleResponse(oAuth2User.getAttributes());
            case "kakao" -> new KakaoResponse(oAuth2User.getAttributes());
            default -> throw new IllegalArgumentException("Unsupported provider: " + registrationId);
        };

        String username = oAuth2Response.getProvider() + " " + oAuth2Response.getProviderId();

        // 1. username으로 조회 → 있으면 UPDATE, 없으면 INSERT
        User user = userRepository.findByUsername(username)
                .orElseGet(() -> {
                    log.info("신규 사용자 생성: {}", username);
                    User newUser = new User();
                    newUser.setUsername(username);
                    newUser.setName(oAuth2Response.getName());
                    newUser.setEmail(oAuth2Response.getEmail());
                    newUser.setRole("ROLE_USER");  // 신규 사용자만 ROLE_USER
                    return userRepository.save(newUser);
                });

        // 2. 기존 사용자면 최신 정보 업데이트 (role은 건드리지 않음!)
        if (user.getId() != null) {  // 이미 DB에 있는 경우
            log.info("기존 사용자 업데이트: {} (role 유지: {})", username, user.getRole());
            user.setName(oAuth2Response.getName());
            user.setEmail(oAuth2Response.getEmail());
            user = userRepository.save(user);  // UPDATE
        }

        // 3. DTO 생성 (role은 DB 그대로)
        UserDTO userDTO = UserDTO.builder()
                .username(user.getUsername())
                .name(user.getName())
                .email(user.getEmail())
                .profileImg(user.getProfileImg())
                .role(user.getRole())  // DB에서 온 그대로
                .build();

        return new CustomOAuth2User(userDTO);
    }
}