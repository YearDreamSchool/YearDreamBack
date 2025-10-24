package elice.yeardreamback.oauth.controller;

import elice.yeardreamback.oauth.jwt.JWTUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * JWT 토큰 관련 요청을 처리하는 컨트롤러입니다.
 * 주로 Refresh Token을 이용한 Access Token 재발급 기능을 담당합니다.
 */
@RestController
@RequestMapping("/api/token")
public class TokenController {

    private final JWTUtil jwtUtil;

    /**
     * 의존성 주입을 위한 생성자입니다.
     * @param jwtUtil JWT 토큰 생성 및 검증 유틸리티
     */
    public TokenController(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * 쿠키에 담긴 리프레시 토큰을 검증하고 새로운 액세스 토큰을 발급합니다.
     * 이 엔드포인트는 Spring Security 설정에서 인증 없이 접근 가능하도록 허용되어야 합니다.
     * @param request HTTP 요청 객체 (쿠키 접근용)
     * @return 성공 시 새 액세스 토큰을 담은 JSON 응답 (HTTP 200 OK), 실패 시 오류 메시지 (HTTP 401 Unauthorized)
     */
    @GetMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {

        // 1. 요청에서 모든 쿠키를 가져옵니다.
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token not found");
        }

        // 2. "refreshToken" 이름의 쿠키 값을 찾습니다.
        String refreshToken = null;
        for (Cookie cookie : cookies) {
            if ("refreshToken".equals(cookie.getName())) {
                refreshToken = cookie.getValue();
                break;
            }
        }

        // 3. 토큰의 유효성 검사 (존재 여부, 만료 여부, 타입 확인)
        if (refreshToken == null || jwtUtil.isExpired(refreshToken) || !jwtUtil.isRefreshToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token invalid or expired");
        }

        // 4. 리프레시 토큰에서 사용자 정보(claim) 추출
        String username = jwtUtil.getUsername(refreshToken);
        String role = jwtUtil.getRole(refreshToken);
        String name = jwtUtil.getName(refreshToken);

        // 5. 새 액세스 토큰 발급 (만료시간 설정)
        long accessExp = 60 * 60 * 1000L; // 1시간
        String newAccessToken = jwtUtil.createJwt("access", username, role, name, accessExp);

        // 6. 새 액세스 토큰을 응답 본문에 담아 반환
        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }
}
