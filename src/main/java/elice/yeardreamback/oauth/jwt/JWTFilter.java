package elice.yeardreamback.oauth.jwt;

import elice.yeardreamback.oauth.dto.CustomOAuth2User;
import elice.yeardreamback.user.dto.UserDTO;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * HTTP 요청당 한 번만 실행되는 커스텀 필터입니다.
 * 요청 헤더에서 JWT 토큰을 추출하고 검증하여 Spring Security Context에 인증 정보를 설정합니다.
 */
@Slf4j
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    /**
     * 의존성 주입을 위한 생성자입니다.
     * @param jwtUtil JWT 토큰 생성, 검증 및 정보 추출 유틸리티
     */
    public JWTFilter(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        // 로그인, 회원가입, 토큰 재발급, Swagger 관련 경로는 필터링하지 않음
        if (path.startsWith("/v3/api-docs/swagger-ui") || path.startsWith("/login")) {
            return true;
        }
        return false;
    }

    /**
     * 필터링 로직을 구현합니다.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 1. Authorization 헤더 가져오기
        String authorizationHeader = request.getHeader("Authorization");
        String token = null;

        // 2. Authorization 헤더가 "Bearer "로 시작하는지 확인하고 토큰 값을 추출합니다.
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
        }

        // 3. 토큰이 없으면 인증 처리 없이 다음 필터로 진행합니다. (인가 로직에서 처리됨)
        if (token == null) {
            filterChain.doFilter(request, response);
            log.debug("Authorization header missing or token is null for request URI: {}", request.getRequestURI());
            return;
        }

        // 4. 토큰 만료 여부를 검증합니다. 만료되었으면 다음 필터로 진행합니다.
        if (jwtUtil.isExpired(token)) {
            log.warn("JWT token expired for request URI: {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        // 5. 토큰에서 사용자 정보(클레임)를 추출합니다.
        String username = jwtUtil.getUsername(token);
        String name = jwtUtil.getName(token);
        String role = jwtUtil.getRole(token);

        // 6. 추출된 정보를 바탕으로 Spring Security용 사용자 DTO를 생성합니다.
        UserDTO userDTO = UserDTO.builder()
                .role(role)
                .name(name)
                .username(username)
                .build();

        // 7. OAuth2 기반 커스텀 사용자 객체를 생성합니다.
        CustomOAuth2User customOAuth2User = new CustomOAuth2User(userDTO);

        // 8. Authentication 객체를 생성하고 Security Context에 설정합니다.
        Authentication authToken = new UsernamePasswordAuthenticationToken(customOAuth2User, null, customOAuth2User.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        // 9. 인증 처리가 완료되었으므로, 다음 필터(또는 최종 DispatcherServlet)로 요청을 전달합니다.
        filterChain.doFilter(request, response);
    }
}