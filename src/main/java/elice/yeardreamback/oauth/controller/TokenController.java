package elice.yeardreamback.oauth.controller;

import elice.yeardreamback.oauth.service.impl.TokenServiceImpl;
import elice.yeardreamback.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * JWT 토큰 관련 요청을 처리하는 컨트롤러입니다.
 * 주로 Refresh Token을 이용한 Access Token 재발급 기능을 담당합니다.
 */
@RestController
@RequestMapping("/api/token")
public class TokenController {

    private final UserService userService;
    private final TokenServiceImpl tokenService;

    /**
     * 의존성 주입을 위한 생성자입니다.
     */
    public TokenController(UserService userService, TokenServiceImpl tokenService) {
        this.userService = userService;
        this.tokenService = tokenService;
    }

    /**
     * 현재 인증된 사용자를 로그아웃 처리합니다.
     * 1. DB에서 사용자의 Refresh Token을 삭제합니다.
     * 2. 요청에 사용된 Access Token을 블랙리스트에 추가하여 즉시 무효화합니다.
     * @param authorizationHeader Authorization 헤더 (Bearer Access Token 포함)
     * @return 성공 메시지 (HTTP 200 OK)
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logoutUser(@RequestHeader("Authorization") String authorizationHeader) {
        // 1. SecurityContext에서 username 가져오기
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // 2. DB에서 Refresh Token 삭제
        userService.logoutUser(username);

        // 3. Access Token 블랙리스트 추가
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String accessToken = authorizationHeader.substring(7);
            tokenService.invalidateToken(accessToken);
        }

        return ResponseEntity.ok("로그아웃이 성공적으로 처리되었습니다.");
    }
}
