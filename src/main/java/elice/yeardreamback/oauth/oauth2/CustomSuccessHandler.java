package elice.yeardreamback.oauth.oauth2;

import elice.yeardreamback.oauth.dto.CustomOAuth2User;
import elice.yeardreamback.oauth.jwt.JWTUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

/**
 * OAuth2 인증(소셜 로그인) 성공 후 실행되는 핸들러입니다.
 * 인증된 사용자 정보를 기반으로 JWT(Access Token 및 Refresh Token)를 생성하고,
 * 클라이언트로 리다이렉트합니다.
 */
@Slf4j
@Component
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;
    private final OAuth2Properties oAuth2Properties;

    /**
     * 의존성 주입을 위한 생성자입니다.
     * @param jwtUtil JWT 토큰 생성 및 처리 유틸리티
     * @param oAuth2Properties OAuth2 관련 설정 프로퍼티
     */
    public CustomSuccessHandler(JWTUtil jwtUtil, OAuth2Properties oAuth2Properties) {
        this.jwtUtil = jwtUtil;
        this.oAuth2Properties = oAuth2Properties;
    }

    /**
     * 인증 성공 시 호출되는 콜백 메서드입니다.
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("===== CustomSuccessHandler 진입 =====");

        // 1. Authentication 객체에서 CustomOAuth2User 객체 추출
        CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        log.info("OAuth2User 추출 완료: {}", customOAuth2User);

        String username = customOAuth2User.getUsername();
        String name = customOAuth2User.getName();
        log.info("사용자 정보 - username: {}, name: {}", username, name);

        // 2. 권한(Role) 정보 추출
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.hasNext() ? iterator.next() : null;
        String role = (auth != null) ? auth.getAuthority() : "ROLE_USER";
        log.info("권한(Role) 정보: {}", role);

        // 3. 토큰 만료 시간 설정
        long accessExpiredMs = 30 * 1000L;
        long refreshExpiredMs = 7 * 24 * 60 * 60 * 1000L;
        log.info("토큰 만료 설정 - access: {}ms, refresh: {}ms", accessExpiredMs, refreshExpiredMs);

        // 4. Access Token 및 Refresh Token 생성
        String accessToken = jwtUtil.createJwt("access", username, role, name, accessExpiredMs);
        String refreshToken = jwtUtil.createJwt("refresh", username, role, name, refreshExpiredMs);
        log.info("JWT 생성 완료 - accessToken: {}, refreshToken: {}", accessToken, refreshToken);

        // 5. Refresh Token을 HTTP Only 쿠키에 저장
        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true); // 로컬 테스트 시 false, 운영 시 true
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge((int) (refreshExpiredMs / 1000));
        response.addCookie(refreshCookie);
        log.info("RefreshToken 쿠키 설정 완료: {}", refreshCookie);

	refreshCookie.setDomain("yeardream.codns.com");

        // 6. Access Token을 쿼리 파라미터로 포함하여 클라이언트(프론트엔드)로 리다이렉트
        String redirectUri = "https://yeardream.site";
//        String redirectUri = "http://localhost:3000";
        log.info("Redirect URI: {}", redirectUri);


        response.sendRedirect(redirectUri + "?token=" + accessToken);
        log.info("리다이렉트 완료: {}?token={}", redirectUri, accessToken);
    }

}
