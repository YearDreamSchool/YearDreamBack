package elice.yeardreamback.oauth.jwt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket 핸드셰이크 과정에서 인증(Authentication)을 처리하는 인터셉터입니다.
 * 클라이언트가 쿼리 파라미터로 전송한 JWT 토큰을 검증하여, 유효할 경우에만 연결을 허용합니다.
 */
@Slf4j
public class AuthHandshakeInterceptor implements HandshakeInterceptor {

    private final JWTUtil jwtUtil;

    /**
     * 의존성 주입을 위한 생성자입니다.
     * @param jwtUtil JWT 토큰 생성 및 검증 유틸리티
     */
    public AuthHandshakeInterceptor(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * WebSocket 연결 핸드셰이크가 시작되기 직전에 호출됩니다.
     * 여기서 JWT 토큰을 검증하여 연결 허용 여부를 결정합니다.
     *
     * @param request 서버 HTTP 요청
     * @param response 서버 HTTP 응답
     * @param wsHandler WebSocket 핸들러
     * @param attributes 핸드셰이크 속성 맵 (WebSocket 세션에 전달될 데이터)
     * @return 토큰이 유효하면 true (연결 허용), 유효하지 않으면 false (연결 거부)
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {

        // 1. 요청 URI의 쿼리 파라미터를 파싱합니다. (예: /ws?token=xxx)
        String query = request.getURI().getQuery();

        if (query != null && query.startsWith("token=")) {
            String token = query.split("=")[1];

            // 2. 토큰 만료 여부를 검증합니다.
            if (!jwtUtil.isExpired(token)) {
                // 3. 토큰이 유효하면 사용자 이름(username)을 WebSocket 세션 속성(attributes)에 저장하고 연결을 허용합니다.
                attributes.put("username", jwtUtil.getUsername(token));
                attributes.put("HANDSHAKE_SUCCESS", true);
                log.info("WebSocket Handshake SUCCESS for user: {}", jwtUtil.getUsername(token));
                return true;
            }
        }

        response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
        log.warn("WebSocket Handshake FAILED: Token invalid or missing. URI: {}", request.getURI());

        return false;
    }

    /**
     * WebSocket 핸드셰이크가 완료된 후(성공 또는 실패) 호출됩니다.
     * attributes에 저장된 플래그와 예외 여부를 통해 결과를 추적하고 정리합니다.
     */
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {}
}