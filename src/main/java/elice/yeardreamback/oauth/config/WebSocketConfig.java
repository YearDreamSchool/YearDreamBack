package elice.yeardreamback.oauth.config;

import elice.yeardreamback.oauth.jwt.AuthHandshakeInterceptor;
import elice.yeardreamback.oauth.jwt.JWTUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket 메시징 구성을 설정합니다.
 * STOMP 프로토콜을 사용하며, 핸드셰이크 시 JWT를 통해 인증을 처리합니다.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JWTUtil jwtUtil;

    /**
     * 의존성 주입을 위한 생성자입니다.
     * @param jwtUtil JWT 토큰 처리 유틸리티
     */
    public WebSocketConfig(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * 클라이언트가 WebSocket 연결을 시작할 엔드포인트를 등록합니다.
     * @param registry STOMP 엔드포인트 등록 레지스트리
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        /**
         * 1. "/ws" 엔드포인트를 통해 WebSocket 연결을 허용합니다.
         * 2. CORS 설정을 통해 localhost:3000에서 오는 요청을 허용합니다.
         * 3. SockJS를 사용하여 WebSocket이 지원되지 않는 환경에서도 연결을 가능하게 합니다.
         * 4. AuthHandshakeInterceptor를 추가하여 JWT를 검증하는 인터셉터를 추가합니다.
         */
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("http://localhost:3000")
                .withSockJS()
                .setInterceptors(new AuthHandshakeInterceptor(jwtUtil)); // 여기서 JWT 체크
    }

    /**
     * 메시지 브로커를 구성합니다.
     * @param registry 메시지 브로커 등록 레지스트리
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }
}
