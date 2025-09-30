package elice.yeardreamback.user.service;

import elice.yeardreamback.user.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 사용자(User)와 관련된 비즈니스 로직을 처리하기 위한 서비스 계층 인터페이스입니다.
 * 사용자 조회, 정보 수정, 로그아웃 등의 핵심 기능을 정의합니다.
 */
@Service
public interface UserService {

    /**
     * 사용자 이름(username)을 기반으로 User 엔티티를 조회합니다.
     * @param username 조회할 사용자의 고유 식별자 (예: 이메일 또는 ID)
     * @return User 엔티티를 담는 {@code Optional<User>} 객체. 해당 사용자가 없으면 empty를 반환합니다.
     */
    Optional<User> findUserByUsername(String username);

    /**
     * 특정 사용자의 정보를 주어진 값으로 수정합니다.
     * @param username 수정 대상 사용자의 고유 식별자
     * @param newName 수정할 새 이름
     * @param newRole 수정할 새 권한
     * @param newEmail 수정할 새 이메일
     * @param newProfileImageUrl 수정할 새 프로필 이미지 URL
     * @param newPhone 수정할 새 전화번호
     * @return 수정 작업이 완료된 User 엔티티 객체
     */
    @Transactional
    User updateUser(String username, String newName, String newRole, String newEmail, String newProfileImageUrl, String newPhone);

    /**
     * 주어진 토큰(일반적으로 Refresh Token)을 무효화하여 사용자를 로그아웃 처리합니다.
     * @param token 무효화할 토큰 문자열
     */
    void logoutUser(String token);
}