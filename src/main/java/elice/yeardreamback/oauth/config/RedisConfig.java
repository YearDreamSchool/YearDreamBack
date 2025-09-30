package elice.yeardreamback.oauth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 데이터베이스 연결 및 RedisTemplate 설정을 정의하는 클래스입니다.
 * application.yml 파일의 spring.data.redis.* 설정을 기반으로 연결을 구성합니다.
 */
@Configuration
public class RedisConfig {

    /**
     * RedisTemplate 빈(Bean)을 생성하고 구성합니다.
     * 이 템플릿은 Java 객체를 Redis에 저장하고 읽어오는 데 사용됩니다.
     * @param connectionFactory Lettuce 기반의 Redis 연결 팩토리 (Spring Boot가 자동 구성)
     * @return 설정이 완료된 RedisTemplate 객체
     */
    @Bean
    public RedisTemplate<String, String> redisTemplate(LettuceConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();

        // 1. 연결 팩토리 설정
        template.setConnectionFactory(connectionFactory);

        // 2. Key 직렬화 방식 설정: Key는 String 타입으로 저장되도록 설정
        template.setKeySerializer(new StringRedisSerializer());

        // 3. Value 직렬화 장식 설저이 Value는 JSON 형식으로 저장되도록 설정
        // (복잡한 Java 객체나 문자열이 아닌 JSON 형태로 저장/복원될 때 유용)
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }
}
