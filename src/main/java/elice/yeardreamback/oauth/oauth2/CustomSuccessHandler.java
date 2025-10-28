//package elice.yeardreamback.oauth.oauth2;
//
//import elice.yeardreamback.oauth.dto.CustomOAuth2User;
//import elice.yeardreamback.oauth.jwt.JWTUtil;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.Cookie;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//import java.util.Collection;
//import java.util.Iterator;
//
///**
// * OAuth2 인증(소셜 로그인) 성공 후 실행되는 핸들러입니다.
// * 인증된 사용자 정보를 기반으로 JWT(Access Token 및 Refresh Token)를 생성하고,
// * 클라이언트로 리다이렉트합니다.
// */
//@Component
//public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
//
//    @Value("${app.oauth2.frontendRedirectUri}")
//    private String frontendRedirectUri;
//
//    private final JWTUtil jwtUtil;
//
//    /**
//     * 의존성 주입을 위한 생성자입니다.
//     * @param jwtUtil JWT 토큰 생성 및 처리 유틸리티
//     */
//    public CustomSuccessHandler(JWTUtil jwtUtil) {
//        this.jwtUtil = jwtUtil;
//    }
//
//    /**
//     * 인증 성공 시 호출되는 콜백 메서드입니다.
//     */
//    @Override
//    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
//
//        // 1. Authentication 객체에서 CustomOAuth2User 객체를 추출합니다.
//        CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();
//
//        String username = customOAuth2User.getUsername();
//        String name = customOAuth2User.getName();
//
//        // 2. 권한(Role) 정보를 추출합니다.
//        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
//        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
//        GrantedAuthority auth = iterator.next(); // 첫 번째 권한만 사용
//        String role = auth.getAuthority();
//
//        // 3. 토큰 만료 시간 설정
//        // 액세스 토큰: 30초
//        long accessExpiredMs = 30 * 1000L;
//        // 리프레시 토큰: 7일
//        long refreshExpiredMs = 7 * 24 * 60 * 60 * 1000L;
//
//        // 4. Access Token 및 Refresh Token 생성
//        String accessToken = jwtUtil.createJwt("access", username, role, name, accessExpiredMs);
//        String refreshToken = jwtUtil.createJwt("refresh", username, role, name, refreshExpiredMs);
//
//        // 5. Refresh Token을 HTTP Only 쿠키에 저장
//        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
//        refreshCookie.setHttpOnly(true); // 💡 XSS 공격 방지를 위해 JavaScript 접근 차단
//        refreshCookie.setSecure(true);  // 💡 HTTPS 환경이 아니라면 false (운영 시 true 권장)
//        refreshCookie.setPath("/");      // 💡 모든 경로에서 쿠키 접근 가능
//        // MaxAge를 밀리초에서 초 단위로 변환
//        refreshCookie.setMaxAge((int) (refreshExpiredMs / 1000));
//        response.addCookie(refreshCookie);
//
//        // 6. Access Token을 쿼리 파라미터로 포함하여 클라이언트(프론트엔드)로 리다이렉트
//        String redirectUrl = frontendRedirectUri + "?token=" + accessToken;
//
//        // 6-1. Swagger UI에서 테스트할 때는 아래 URL로 리다이렉트
//        String swaggerRedirectUrl = "http://localhost:8080/v3/api-docs/swagger-ui/oauth-redirect.html?token=" + accessToken;
//
//
//        response.sendRedirect(redirectUrl);
//    }
//}

package elice.yeardreamback.oauth.oauth2;

import elice.yeardreamback.oauth.dto.CustomOAuth2User;
import elice.yeardreamback.oauth.jwt.JWTUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

@Component
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(CustomSuccessHandler.class);

    @Value("${app.oauth2.frontendRedirectUri}")
    private String frontendRedirectUri;

    private final JWTUtil jwtUtil;

    public CustomSuccessHandler(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // --- MDC로 요청 식별자 추가 (로그 추적 용이) ---
        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);

        try {
            log.info("OAuth2 authentication success handler started. remoteAddr={}, requestId={}",
                    request.getRemoteAddr(), requestId);

            // 1. Principal 추출
            Object principal = authentication.getPrincipal();
            if (!(principal instanceof CustomOAuth2User)) {
                log.warn("Authentication principal is not CustomOAuth2User. principalClass={}, requestId={}",
                        principal != null ? principal.getClass().getName() : "null", requestId);
                super.onAuthenticationSuccess(request, response, authentication);
                return;
            }
            CustomOAuth2User customOAuth2User = (CustomOAuth2User) principal;

            String username = customOAuth2User.getUsername();
            String name = customOAuth2User.getName();
            log.debug("Authenticated user info extracted: username={}, name={}, requestId={}", username, name, requestId);

            // 2. 권한(Role) 정보 추출
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
            String role = iterator.hasNext() ? iterator.next().getAuthority() : "ROLE_ANONYMOUS";
            log.debug("User role resolved: role={}, username={}, requestId={}", role, username, requestId);

            // 3. 토큰 만료 시간
            long accessExpiredMs = 30 * 1000L;
            long refreshExpiredMs = 7 * 24 * 60 * 60 * 1000L;

            // 4. 토큰 생성
            String accessToken = jwtUtil.createJwt("access", username, role, name, accessExpiredMs);
            String refreshToken = jwtUtil.createJwt("refresh", username, role, name, refreshExpiredMs);

            // 토큰 자체는 절대 원문으로 로그에 남기지 않음 — 마스킹해서 일부만 기록
            log.info("JWTs created for user. username={}, accessTokenPartial={}, refreshTokenExpiryMs={}, requestId={}",
                    username,
                    maskToken(accessToken),
                    refreshExpiredMs,
                    requestId);

            // 5. Refresh Token을 쿠키에 저장
            Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
            refreshCookie.setHttpOnly(true);
            // 개발 환경에서 HTTPS가 아닐 경우 false로 설정해둔 상태라면 다음 로그로 알림
            refreshCookie.setSecure(true);
            refreshCookie.setPath("/");
            refreshCookie.setMaxAge((int) (refreshExpiredMs / 1000));
            response.addCookie(refreshCookie);
            log.debug("Refresh token cookie added. cookiePath={}, maxAgeSec={}, requestId={}",
                    refreshCookie.getPath(), refreshCookie.getMaxAge(), requestId);

            // 6. Redirect (Access token을 쿼리로 전달 — 배포 시 주의)
            String redirectUrl = frontendRedirectUri + "?token=" + accessToken;
            // 로그에는 전체 토큰을 찍지 말고, 리다이렉트 대상과 토큰 일부만 기록
            log.info("Redirecting to frontend. redirectBase={}, tokenPartial={}, requestId={}",
                    frontendRedirectUri, maskToken(accessToken), requestId);

            // 실제로 리다이렉트
            response.sendRedirect(redirectUrl);
            log.info("Redirect sent. requestId={}", requestId);

        } catch (Exception e) {
            log.error("Exception in onAuthenticationSuccess. requestId={}", MDC.get("requestId"), e);
            throw e;
        } finally {
            MDC.remove("requestId");
        }
    }

    /**
     * 토큰을 안전하게 로그에 남기기 위해 앞/뒤 일부만 표시하고 나머지는 '*'로 마스킹합니다.
     */
    private String maskToken(String token) {
        if (token == null) return "null";
        int show = 6;
        if (token.length() <= show * 2) return "****";
        String start = token.substring(0, show);
        String end = token.substring(token.length() - show);
        return start + "......" + end;
    }
}
