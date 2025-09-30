package elice.yeardreamback.oauth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT(JSON Web Token)를 생성, 검증 및 파싱하는 유틸리티 클래스입니다.
 * 애플리케이션의 인증 및 인가에 필요한 모든 토큰 관련 로직을 처리합니다.
 */
@Component
public class JWTUtil {

    // JWT 서명 및 검증에 사용되는 비밀 키
    private SecretKey secretKey;

    /**
     * SecretKey를 주입받아 초기화합니다.
     * @param secret application.yml 또는 환경 변수에서 주입받은 비밀 문자열
     */
    public JWTUtil(@Value("${spring.jwt.secret}")String secret) {
        secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    /**
     * 토큰에서 사용자 이름(username) 클레임을 추출합니다.
     * @param token 파싱할 JWT 문자열
     * @return 사용자 이름 (String)
     */
    public String getUsername(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("username", String.class);
    }

    /**
     * 토큰에서 사용자 실명 또는 표시 이름(name) 클레임을 추출합니다.
     * @param token 파싱할 JWT 문자열
     * @return 사용자 이름 (String)
     */
    public String getName(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("name", String.class);
    }

    /**
     * 토큰에서 사용자 역할(role) 클레임을 추출합니다.
     * @param token 파싱할 JWT 문자열
     * @return 사용자 역할 (String, 예: "USER", "ADMIN")
     */
    public String getRole(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("role", String.class);
    }

    /**
     * 토큰의 만료 여부를 확인합니다.
     * @param token 검증할 JWT 문자열
     * @return 토큰이 현재 시간보다 이전에 만료되었다면 true, 아니면 false
     */
    public Boolean isExpired(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration().before(new Date());
    }

    /**
     * 액세스 토큰(Access Token)을 생성합니다.
     * @param tokenType 토큰의 유형 (예: "access")
     * @param username 사용자 식별자
     * @param role 사용자 역할
     * @param name 사용자 이름
     * @param expiredMs 토큰의 유효 기간 (밀리초)
     * @return 서명된 JWT 문자열
     */
    public String createJwt(String tokenType, String username, String role, String name, Long expiredMs) {
        return Jwts.builder()
                .claim("tokenType", tokenType)
                .claim("username", username)
                .claim("name", name)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }

    /**
     * 리프레시 토큰(Refresh Token)을 생성합니다. (Access Token 생성과 클레임 구조는 동일하지만, 보통 더 긴 만료 기간을 가집니다.)
     * @param tokenType 토큰의 유형 (예: "refresh")
     * @param username 사용자 식별자
     * @param role 사용자 역할
     * @param name 사용자 이름
     * @param expiredMs 토큰의 유효 기간 (밀리초)
     * @return 서명된 JWT 문자열
     */
    public String createRefreshJwt(String tokenType, String username, String role, String name, Long expiredMs) {
        return Jwts.builder()
                .claim("tokenType", tokenType)
                .claim("username", username)
                .claim("role", role)
                .claim("name", name)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }

    /**
     * 주어진 토큰이 Refresh Token인지 확인합니다.
     * 토큰을 파싱하여 "tokenType" 클레임이 "refresh"인지 검사합니다.
     * @param token 검증할 JWT 문자열
     * @return 토큰 유형이 "refresh"이면 true, 아니면 false (서명 오류 포함)
     */
    public Boolean isRefreshToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return "refresh".equals(claims.get("tokenType", String.class));
        } catch (Exception e) {
            return false;
        }
    }
}
