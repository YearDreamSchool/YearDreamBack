package elice.yeardreamback.oauth.service.impl;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;

/**
 * JWT 토큰의 무효화(블랙리스트) 관리를 담당하는 서비스 구현체입니다.
 * Redis를 사용하여 무효화된 토큰을 저장하고 유효성을 검사합니다.
 */
@Service
public class TokenServiceImpl {

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 의존성 주입을 위한 생성자입니다.
     * @param redisTemplate String 타입의 Key와 Value를 사용하는 RedisTemplate (RedisConfig에서 정의되었거나 StringRedisTemplate이 주입됨)
     */
    public TokenServiceImpl(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 주어진 토큰을 블랙리스트에 추가하여 무효화합니다.
     * 블랙리스트 항목은 만료 시간을 설정하여 메모리 효율성을 확보합니다.
     * @param token 블랙리스트에 추가할 JWT 문자열 (일반적으로 Refresh Token 또는 Access Token)
     */
    public void invalidateToken(String token) {
        long expirationTime = 3600;

        redisTemplate.opsForValue().set("blacklist:" + token, "logout", Duration.ofSeconds(expirationTime));
    }

    /**
     * 주어진 토큰이 블랙리스트에 등록되어 무효화된 상태인지 확인합니다.
     * @param token 확인할 JWT 문자열
     * @return 블랙리스트 키가 존재하면(무효화된 상태) true, 아니면 false
     */
    public boolean isTokenBlacklisted(String token) {
        return redisTemplate.hasKey("blacklist:" + token);
    }
}
