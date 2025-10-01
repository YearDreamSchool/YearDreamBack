package elice.yeardreamback.user.repository;

import elice.yeardreamback.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.util.Optional;

/**
 * User 엔티티에 대한 데이터베이스 접근을 담당하는 리포지토리 인터페이스입니다.
 * Spring Data JPA의 JpaRepository를 상속받아 기본적인 CRUD 기능을 제공받습니다.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 사용자 이름(username)을 기준으로 User 엔티티를 조회합니다.
     * * @param username 조회할 사용자의 고유 식별자 (로그인 ID)
     * @return 주어진 username과 일치하는 User 엔티티를 담는 Optional 객체.
     * 사용자가 존재하지 않으면 Optional.empty()를 반환합니다.
     */
    Optional<User> findByUsername(String username);

    /**
     * 이메일(email)을 기준으로 User 엔티티를 조회합니다.
     * @param email
     * @return 주어진 email과 일치하는 User 엔티티를 담는 Optional 객체.
     */
    Optional<User> findByEmail(String email);
}