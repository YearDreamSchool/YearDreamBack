package elice.yeardreamback.oauth.controller;

import elice.yeardreamback.oauth.jwt.JWTUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/token")
public class TokenController {

    private final JWTUtil jwtUtil;

    public TokenController(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * 쿠키에 담긴 리프레시 토큰으로 새 액세스 토큰 발급
     */
    @GetMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token not found");
        }

        String refreshToken = null;
        for (Cookie cookie : cookies) {
            if ("refreshToken".equals(cookie.getName())) {
                refreshToken = cookie.getValue();
                break;
            }
        }

        if (refreshToken == null || jwtUtil.isExpired(refreshToken) || !jwtUtil.isRefreshToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token invalid or expired");
        }

        // 리프레시 토큰에서 사용자 정보 가져오기
        String username = jwtUtil.getUsername(refreshToken);
        String role = jwtUtil.getRole(refreshToken);
        String name = jwtUtil.getName(refreshToken);

        // 새 액세스 토큰 발급
        long accessExp = 60 * 60 * 1000L; // 1시간
        String newAccessToken = jwtUtil.createJwt("access", username, role, name, accessExp);

        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }
}
