package elice.yeardreamback.oauth.oauth2;

import elice.yeardreamback.oauth.dto.CustomOAuth2User;
import elice.yeardreamback.oauth.entity.RefreshToken;
import elice.yeardreamback.oauth.jwt.JWTUtil;
import elice.yeardreamback.oauth.repository.RefreshRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;

/**
 * OAuth2 인증(소셜 로그인) 성공 후 실행되는 핸들러입니다.
 * JWT를 생성하고 Refresh Token을 SameSite=None 쿠키로 발급합니다.
 */
@Slf4j
@Component
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;
    private final OAuth2Properties oAuth2Properties;
    private final RefreshRepository refreshRepository;

    public CustomSuccessHandler(JWTUtil jwtUtil,
                                OAuth2Properties oAuth2Properties,
                                RefreshRepository refreshRepository) {
        this.jwtUtil = jwtUtil;
        this.oAuth2Properties = oAuth2Properties;
        this.refreshRepository = refreshRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        log.info("===== CustomSuccessHandler 진입 =====");

        // 1. 사용자 정보 추출
        CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        log.info("OAuth2User 추출 완료: {}", customOAuth2User);

        String username = customOAuth2User.getUsername();
        String name = customOAuth2User.getName();
        String role = customOAuth2User.getRole();
        String email = customOAuth2User.getEmail();
        log.info("사용자 정보 - username: {}, name: {}, role: {}, email: {}", username, name, role, email);

        // 2. 토큰 만료 시간 설정
        long accessExpiredMs = 15 * 60 * 1000L;             // 15분
        long refreshExpiredMs = 7L * 24 * 60 * 60 * 1000;   // 7일
        log.info("토큰 만료 설정 - access: {}ms, refresh: {}ms", accessExpiredMs, refreshExpiredMs);

        // 3. JWT 생성
        String accessToken = jwtUtil.createJwt("access", username, role, name, accessExpiredMs);
        String refreshToken = jwtUtil.createJwt("refresh", username, role, name, refreshExpiredMs);
        log.info("JWT 생성 완료");

        // 4. RefreshToken DB 저장
        RefreshToken tokenEntity = new RefreshToken();
        tokenEntity.setUsername(username);
        tokenEntity.setRefresh(refreshToken);
        tokenEntity.setExpiration(String.valueOf(System.currentTimeMillis() + refreshExpiredMs));
        refreshRepository.save(tokenEntity);

        // 5. Refresh Token을 SameSite=None 쿠키로 발급 (ResponseCookie 사용)
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)                     // JS 접근 차단
                .secure(true)                       // HTTPS 전용
                .path("/")                          // 전체 경로
                .maxAge(Duration.ofDays(7))         // 7일
                .domain(".yeardream.codns.com")       // 서브도메인 포함
//                .domain(".localhost")               // 로컬 테스트용
                .sameSite("None")                   // 크로스 사이트 허용 (필수!)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        log.info("RefreshToken 쿠키 발급 완료 (SameSite=None, Domain=.yeardream.site)");

        // 6. Access Token을 쿼리 파라미터로 리다이렉트
        String redirectUri = "https://yeardream.site";
        // 로컬 테스트용: String redirectUri = "http://localhost:3000";

        log.info("리다이렉트 URI: {}", redirectUri);
        response.sendRedirect(redirectUri + "?token=" + accessToken);
        log.info("리다이렉트 완료");
    }
}