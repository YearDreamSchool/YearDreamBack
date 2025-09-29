package elice.yeardreamback.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

/**
 * Swagger OpenAPI 설정
 * 전체 애플리케이션의 API 문서화를 위한 설정
 */
@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(createApiInfo())
                .servers(createServers())
                .tags(createTags())
                .components(createComponents())
                .security(createSecurityRequirements());
    }

    /**
     * API 기본 정보 설정
     */
    private Info createApiInfo() {
        return new Info()
                .title("YearDream Backend API")
                .description("""
                    YearDream 백엔드 서비스 API 문서
                    
                    ## 주요 기능
                    - 사용자 인증 및 권한 관리
                    - 캘린더 이벤트 관리
                    - 이벤트 카테고리 관리
                    - 이벤트 공유 및 알림 기능
                    
                    ## 인증 방식
                    - JWT Bearer Token 인증
                    - OAuth2 소셜 로그인 (Google, Naver, Kakao)
                    
                    ## API 사용 가이드
                    1. 먼저 로그인하여 JWT 토큰을 획득하세요
                    2. 모든 API 요청 시 Authorization 헤더에 'Bearer {token}' 형식으로 토큰을 포함하세요
                    3. 날짜/시간은 ISO 8601 형식(YYYY-MM-DDTHH:mm:ss)을 사용하세요
                    """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("YearDream 개발팀")
                        .email("dev@yeardream.com")
                        .url("https://yeardream.com"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }

    /**
     * 서버 정보 설정
     */
    private List<Server> createServers() {
        return Arrays.asList(
                new Server()
                        .url("http://localhost:8080")
                        .description("개발 서버 (로컬)"),
                new Server()
                        .url("https://dev-api.yeardream.com")
                        .description("개발 서버 (원격)"),
                new Server()
                        .url("https://api.yeardream.com")
                        .description("운영 서버")
        );
    }

    /**
     * API 태그 정의
     */
    private List<Tag> createTags() {
        return Arrays.asList(
                new Tag()
                        .name("Authentication")
                        .description("사용자 인증 및 권한 관리 API"),
                new Tag()
                        .name("User Management")
                        .description("사용자 정보 관리 API"),
                new Tag()
                        .name("Calendar Events")
                        .description("캘린더 이벤트 관리 API - 이벤트 생성, 조회, 수정, 삭제 및 다양한 필터링 기능"),
                new Tag()
                        .name("Event Categories")
                        .description("이벤트 카테고리 관리 API - 카테고리 생성, 관리 및 이벤트 분류 기능"),
                new Tag()
                        .name("Event Sharing")
                        .description("이벤트 공유 관리 API - 다른 사용자와의 이벤트 공유 및 권한 관리"),
                new Tag()
                        .name("Event Reminders")
                        .description("이벤트 알림 관리 API - 이벤트 알림 설정 및 관리")
        );
    }

    /**
     * 보안 스키마 및 컴포넌트 설정
     */
    private Components createComponents() {
        return new Components()
                .addSecuritySchemes("bearerAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT 토큰을 사용한 인증. Authorization 헤더에 'Bearer {token}' 형식으로 입력하세요."))
                .addSecuritySchemes("oauth2", new SecurityScheme()
                        .type(SecurityScheme.Type.OAUTH2)
                        .description("OAuth2 소셜 로그인 (Google, Naver, Kakao)"));
    }

    /**
     * 보안 요구사항 설정
     */
    private List<SecurityRequirement> createSecurityRequirements() {
        return Arrays.asList(
                new SecurityRequirement().addList("bearerAuth")
        );
    }
}