package elice.yeardreamback.oauth.repository;

import elice.yeardreamback.oauth.entity.RefreshToken;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * RefreshToken 엔티티에 대한 데이터베이스 접근(CRUD) 기능을 제공하는 JPARepository 인터페이스입니다.
 * JWT 기반 인증 시스템에서 Refresh Token의 관리(저장, 조회, 삭제)를 수항합니다.
 */
@Repository
public interface RefreshRepository extends JpaRepository<RefreshToken, Integer> {

    /**
     * 주어진 사용자 이름(username)에 해당하는 Refresh Token 엔티티를 데이터베이스에서 조회합니다.
     * @param username 조회할 사용자의 고유 이름
     */
    RefreshToken findByUsername(String username);

    /**
     * 주어진 Refresh Token 문자열(refresh)을 가진 엔티티가 데이터베이스에 존재하는지 확인합니다.
     * @param refresh 검증할 Refresh Token 문자열
     * @return 존재하면 true, 아니면 false
     */
    Boolean existsByRefresh(String refresh);

    /**
     * 주어진 Refresh Token 문자열(refresh)을 가진 엔티티를 데이터베이스에서 삭제합니다.
     * 이 메서드는 토큰 무효화(로그아웃) 시 사용됩니다.
     * @param refresh 삭제할 Refresh Token 문자열
     */
    @Transactional
    void deleteByRefresh(String refresh);

    /**
     * 주어진 사용자 이름(username)에 해당하는 Refresh Token 엔티티를 데이터베이스에서 삭제합니다.
     * @param username 삭제할 토큰을 소유한 사용자의 고유 이름
     */
    @Transactional
    void deleteByUsername(String username);
}