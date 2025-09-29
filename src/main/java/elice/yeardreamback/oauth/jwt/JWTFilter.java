package elice.yeardreamback.oauth.jwt;

import elice.yeardreamback.oauth.dto.CustomOAuth2User;
import elice.yeardreamback.user.dto.UserDTO;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    public JWTFilter(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // Authorization 헤더 가져오기
        String authorizationHeader = request.getHeader("Authorization");
        String token = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
        }

        if (token == null) {
            filterChain.doFilter(request, response);
            System.out.println("token null");
            return;
        }

        if (jwtUtil.isExpired(token)) {
            System.out.println("token expired");
            filterChain.doFilter(request, response);
            return;
        }

        // 토큰에서 정보 추출
        String username = jwtUtil.getUsername(token);
        String name = jwtUtil.getName(token);
        String role = jwtUtil.getRole(token);

        // CustomOAuth2User 생성
        UserDTO userDTO = UserDTO.builder()
                .role(role)
                .name(name)
                .username(username)
                .build();

        CustomOAuth2User customOAuth2User = new CustomOAuth2User(userDTO);

        // Authentication 세팅
        Authentication authToken = new UsernamePasswordAuthenticationToken(customOAuth2User, null, customOAuth2User.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }
}