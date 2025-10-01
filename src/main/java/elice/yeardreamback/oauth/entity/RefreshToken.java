package elice.yeardreamback.oauth.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;


/**
 * Refresh Token 정보를 저장하는 엔티티 클래스입니다.
 * 이 토큰은 Access Token이 만료되었을 때 새로운 Access Token을 발급받는 데 사용되며,
 * 보안을 위해 데이터베이스에 저장되어 관리됩니다.
 */
@Entity
public class RefreshToken {

    /**
     * 기본 키 (Primary Key). 자동 증가 전략을 사용하는 정수형 ID입니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    /**
     * Refresh Token을 소유한 사용자의 고유 식별자(username)입니다.
     * 이 필드를 통해 사용자와 토큰을 매핑합니다.
     */
    private String username;

    /**
     * 실제 Refresh Token 문자열입니다.
     */
    private String refresh;

    /**
     * Refresh Token의 만료 시각(Expiration Time)을 나타내는 문자열입니다.
     * 보통 Unix Time Stamp 또는 ISO 8601 형식의 문자열로 저장됩니다.
     */
    private String expiration;
}