package elice.yeardreamback.oauth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 설정을 커스터마이징하여 전역 CORS(Cross-Origin Resource Sharing) 규칙을 정의하는 클래스입니다.
 */
@Configuration
public class CorsMvcConfig implements WebMvcConfigurer {

    /**
     * 전역 CORS 매핑 설정을 추가합니다.
     * @param corsRegistry CORS 설정을 관리하는 레지스트리
     */
    @Override
    public void addCorsMappings(CorsRegistry corsRegistry) {

        // 1. 모든 경로(/**)에 대해 CORS를 적용합니다.
        corsRegistry.addMapping("/**")

                // 2. 클라이언트가 접근할 수 있도록 노출할 응답 헤더를 지정합니다.
                // JWT 토큰 등이 담긴 Set-Cookie 헤더를 클라이언트에서 읽을 수 있도록 허용합니다.
                .exposedHeaders("Set-Cookie")

                // 3. 리소스 접근을 허용할 출처(Origin)를 지정합니다.
                // 프론트엔드 서버의 주소인 http://localhost:3000을 허용합니다.
                .allowedOrigins("http://localhost:3000")

                // 4. 인증 정보(Credentials), 즉 쿠키(Cookies), HTTP 인증 등을 요청에 포함할 수 있도록 허용합니다.
                // Refresh Token을 쿠키로 주고받을 때 필수 설정입니다.
                .allowCredentials(true)

                // 5. 허용할 HTTP 메서드들을 지정합니다.
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
    }
}
