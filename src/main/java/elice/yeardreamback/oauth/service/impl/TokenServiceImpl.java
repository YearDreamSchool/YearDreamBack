package elice.yeardreamback.oauth.service.impl;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;

@Service
public class TokenServiceImpl {

    private final RedisTemplate<String, String> redisTemplate;

    public TokenServiceImpl(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void invalidateToken(String token) {
        long expirationTime = 3600;

        redisTemplate.opsForValue().set("blacklist:" + token, "logout", Duration.ofSeconds(expirationTime));
    }

    public boolean isTokenBlacklisted(String token) {
        return redisTemplate.hasKey("blacklist:" + token);
    }
}
