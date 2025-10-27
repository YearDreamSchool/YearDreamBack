package elice.yeardreamback.oauth.oauth2;

import elice.yeardreamback.oauth.dto.CustomOAuth2User;
import elice.yeardreamback.oauth.jwt.JWTUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
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
@Component
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Value("${app.oauth2.frontendRedirectUri}")
    private String frontendRedirectUri;

    private final JWTUtil jwtUtil;

    /**
     * 의존성 주입을 위한 생성자입니다.
     * @param jwtUtil JWT 토큰 생성 및 처리 유틸리티
     */
    public CustomSuccessHandler(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * 인증 성공 시 호출되는 콜백 메서드입니다.
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        // 1. Authentication 객체에서 CustomOAuth2User 객체를 추출합니다.
        CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        String username = customOAuth2User.getUsername();
        String name = customOAuth2User.getName();

        // 2. 권한(Role) 정보를 추출합니다.
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next(); // 첫 번째 권한만 사용
        String role = auth.getAuthority();

        // 3. 토큰 만료 시간 설정
        // 액세스 토큰: 30초
        long accessExpiredMs = 30 * 1000L;
        // 리프레시 토큰: 7일
        long refreshExpiredMs = 7 * 24 * 60 * 60 * 1000L;

        // 4. Access Token 및 Refresh Token 생성
        String accessToken = jwtUtil.createJwt("access", username, role, name, accessExpiredMs);
        String refreshToken = jwtUtil.createJwt("refresh", username, role, name, refreshExpiredMs);

        // 5. Refresh Token을 HTTP Only 쿠키에 저장
        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
        refreshCookie.setHttpOnly(true); // 💡 XSS 공격 방지를 위해 JavaScript 접근 차단
        refreshCookie.setSecure(false);  // 💡 HTTPS 환경이 아니라면 false (운영 시 true 권장)
        refreshCookie.setPath("/");      // 💡 모든 경로에서 쿠키 접근 가능
        // MaxAge를 밀리초에서 초 단위로 변환
        refreshCookie.setMaxAge((int) (refreshExpiredMs / 1000));
        response.addCookie(refreshCookie);

        // 6. Access Token을 쿼리 파라미터로 포함하여 클라이언트(프론트엔드)로 리다이렉트
        String redirectUrl = frontendRedirectUri + "?token=" + accessToken;

        // 6-1. Swagger UI에서 테스트할 때는 아래 URL로 리다이렉트
        String swaggerRedirectUrl = "http://localhost:8080/v3/api-docs/swagger-ui/oauth-redirect.html?token=" + accessToken;


        response.sendRedirect(redirectUrl);
    }
}