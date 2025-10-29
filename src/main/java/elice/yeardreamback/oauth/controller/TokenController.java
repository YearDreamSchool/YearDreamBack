package elice.yeardreamback.oauth.controller;

import elice.yeardreamback.oauth.jwt.JWTUtil;
import elice.yeardreamback.user.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Map;

public class TokenController {

    private final JWTUtil jwtUtil;
    private final UserService userService;

    /**
     * 의존성 주입을 위한 생성자입니다.
     *
     * @param jwtUtil JWT 토큰 생성 및 검증 유틸리티
     */
    public TokenController(JWTUtil jwtUtil, UserService userService) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("리프레시 토큰이 없습니다.");
        }

        String refreshToken = null;
        for (Cookie cookie : cookies) {
            if ("refreshToken".equals(cookie.getName())) {
                refreshToken = cookie.getValue();
                break;
            }
        }

        if (refreshToken == null || jwtUtil.isExpired(refreshToken) || !jwtUtil.isRefreshToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("리프레시 토큰이 유효하지 않습니다.");
        }

        String username = jwtUtil.getUsername(refreshToken);
        String role = jwtUtil.getRole(refreshToken);
        String name = jwtUtil.getName(refreshToken);

        String newAccessToken = jwtUtil.createJwt("access", username, role, name, 60 * 60 * 1000L);

        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }
}
