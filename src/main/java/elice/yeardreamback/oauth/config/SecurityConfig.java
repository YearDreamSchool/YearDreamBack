package elice.yeardreamback.oauth.config;

import elice.yeardreamback.oauth.jwt.JWTFilter;
import elice.yeardreamback.oauth.jwt.JWTUtil;
import elice.yeardreamback.oauth.oauth2.CustomSuccessHandler;
import elice.yeardreamback.oauth.service.CustomOAuth2UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Collections;

/**
 * Spring Security 설정을 정의하는 클래스입니다.
 * OAuth2 로그인, JWT 인증 필터, CORS, CSRF 및 세션 정책 등을 구성합니다.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomSuccessHandler customSuccessHandler;
    private final JWTUtil jwtUtil;

    public SecurityConfig(CustomOAuth2UserService customOAuth2UserService, CustomSuccessHandler customSuccessHandler, JWTUtil jwtUtil) {
        this.customOAuth2UserService = customOAuth2UserService;
        this.customSuccessHandler = customSuccessHandler;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Spring Security의 필터 체인을 구성하는 핵심 Bean입니다.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        /**
         * CORS 설정을 통해 특정 출처에서 오는 요청을 허용합니다.
         * 여기서는 http://localhost:3000에서 오는 모든 메서드와 헤더를 허용하고,
         * 인증 정보(쿠키, 인증 헤더 등)를 포함한 요청을 허용합니다.
         * 또한, 클라이언트가 응답에서 Authorization 헤더에 접근할 수 있도록 설정합니다.
         */
        http
                .cors(corsCustomizer -> corsCustomizer.configurationSource(new CorsConfigurationSource() {

                    @Override
                    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {

                        CorsConfiguration configuration = new CorsConfiguration();

                        configuration.setAllowedOrigins(Collections.singletonList("http://localhost:3000"));
                        configuration.setAllowedMethods(Collections.singletonList("*"));
                        configuration.setAllowCredentials(true);
                        configuration.setAllowedHeaders(Collections.singletonList("*"));
                        configuration.setMaxAge(3600L);

                        // 클라이언트가 응답에서 접근할 수 있도록 Authorization 헤더 노출
                        configuration.setExposedHeaders(Collections.singletonList("Authorization"));

                        return configuration;
                    }
                }));

        /**
         * CSRF 보호 비활성화 및 프레임 옵션 설정입니다.
         * 개발중이기에 CSRF 보호를 비활성화하고,
         * H2 콘솔 사용을 위해 프레임 옵션을 동일 출처로 설정합니다.
         */
        http
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
            );

        /**
         * 폼 로그인 비활성화
         */
        http
                .formLogin((auth) -> auth.disable());

        /**
         * HTTP Basic 인증 비활성화
         */
        http
                .httpBasic((auth) -> auth.disable());

        /**
         * JWT 필터를 UsernamePasswordAuthenticationFilter 전에 추가합니다.
         */
        http
                .addFilterBefore(new JWTFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);

        /**
         * OAUTH2 로그인 설정입니다.
         * 사용자 정보는 customOAuth2UserService를 통해 가져오고,
         * 로그인 성공 시 customSuccessHandler가 처리합니다.
         */
        http
                .oauth2Login((auth) -> auth
                        .userInfoEndpoint((userInfoEndpointConfig) -> userInfoEndpointConfig
                                .userService(customOAuth2UserService))
                        .successHandler(customSuccessHandler));

        /**
         * 인가(Authorization) 설정입니다.
         * 특정 경로들은 모두에게 허용하고, 그 외의 요청은 인증된 사용자만 접근할 수 있도록 설정합니다.
         */
        http
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers(
                                "/",
                                "/login/**",
                                "/api/token/google",
                                "/ws/**",
                                "/h2-console/**",
                                "/swagger-ui/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/oauth2/authorization/**",
                                "/api/token/refresh"
                        ).permitAll()
                        .anyRequest().authenticated()
                );

        /**
         * 세션 정책을 STATELESS로 설정하여 서버가 세션을 생성하거나 사용하지 않도록 합니다.
         */
        http
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        /**
         * 인증 실패 시 401 Unauthorized 응답을 반환하도록 설정합니다.
         * 응답은 JSON 형식으로 반환됩니다.
         */
        http
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"error\":\"Unauthorized\"}");
                        })
                );

        return http.build();
    }
}
