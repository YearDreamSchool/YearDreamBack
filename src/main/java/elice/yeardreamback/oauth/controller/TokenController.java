package elice.yeardreamback.oauth.controller;

import elice.yeardreamback.oauth.jwt.JWTUtil;
import elice.yeardreamback.user.service.UserService;
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
    private final UserService userService;

    /**
     * 의존성 주입을 위한 생성자입니다.
     * @param jwtUtil JWT 토큰 생성 및 검증 유틸리티
     */
    public TokenController(JWTUtil jwtUtil, UserService userService) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    /**
     * 쿠키에 담긴 리프레시 토큰을 검증하고 새로운 액세스 토큰을 발급합니다.
     * 이 엔드포인트는 Spring Security 설정에서 인증 없이 접근 가능하도록 허용되어야 합니다.
     * @param request HTTP 요청 객체 (쿠키 접근용)
     * @return 성공 시 새 액세스 토큰을 담은 JSON 응답 (HTTP 200 OK), 실패 시 오류 메시지 (HTTP 401 Unauthorized)
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logoutUser(
            @RequestHeader("Authorization") String authorizationHeader) {

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("Authorization 헤더가 올바르지 않습니다.");
        }

        // Bearer 토큰에서 refreshToken 추출
        String refreshToken = authorizationHeader.substring(7);

        // DB에서 refreshToken 삭제
        userService.logoutUser(refreshToken);

        return ResponseEntity.ok("로그아웃이 성공적으로 처리되었습니다.");
    }
}
