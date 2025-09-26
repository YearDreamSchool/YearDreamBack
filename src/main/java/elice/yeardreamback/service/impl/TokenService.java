package elice.yeardreamback.service.impl;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;

@Service
public class TokenService {

    private final RedisTemplate<String, String> redisTemplate;

    public TokenService(RedisTemplate<String, String> redisTemplate) {
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
