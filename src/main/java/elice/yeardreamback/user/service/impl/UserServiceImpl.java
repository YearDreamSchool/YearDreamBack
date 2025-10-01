package elice.yeardreamback.user.service.impl;

import elice.yeardreamback.user.entity.User;
import elice.yeardreamback.user.exception.DuplicateEmailException;
import elice.yeardreamback.user.exception.UserNotFoundException;
import elice.yeardreamback.user.repository.UserRepository;
import elice.yeardreamback.oauth.service.impl.TokenServiceImpl;
import elice.yeardreamback.user.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * UserService 인터페이스의 구현체입니다.
 * 사용자 관련 비즈니스 로직을 처리합니다.
 */
@Service
public class UserServiceImpl implements UserService {

    private final TokenServiceImpl tokenServiceImpl;
    private final UserRepository userRepository;

    /**
     * 의존성 주입을 위한 생성자입니다.
     * @param tokenServiceImpl 토큰 관련 서비스 (주로 Refresh Token 무효화에 사용)
     * @param userRepository 사용자 엔티티 데이터 접근 리포지토리
     */
    public UserServiceImpl(TokenServiceImpl tokenServiceImpl, UserRepository userRepository) {
        this.tokenServiceImpl = tokenServiceImpl;
        this.userRepository = userRepository;
    }

    /**
     * 사용자 이름(username)으로 User 엔티티를 조회합니다.
     * @param username 조회할 사용자의 고유 식별자
     * @return User 엔티티를 담는 Optional 객체
     */
    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * 특정 사용자의 정보를 수정합니다.
     * UserNotFoundException이 발생하면 트랜잭션은 롤백됩니다.
     * @param username 수정 대상 사용자의 고유 식별자
     * @param newName 수정할 이름
     * @param newRole 수정할 권한
     * @param newEmail 수정할 이메일
     * @param newProfileImageUrl 수정할 프로필 이미지 URL
     * @param newPhone 수정할 전화번호
     * @return 수정된 User 엔티티 객체
     * @throws UserNotFoundException 해당 username을 가진 사용자가 없을 경우
     */
    @Transactional
    public User updateUser(String username, String newName, String newRole, String newEmail, String newProfileImageUrl, String newPhone) {
        // 사용자 조회. 없으면 UserNotFoundException을 발생시키고 트랜잭션 롤백
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));

        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("이름은 필수 입력 항목입니다.");
        }
        if (newEmail == null || newEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("이메일은 필수 입력 항목입니다.");
        }
        if (!user.getEmail().equals(newEmail)) {
            userRepository.findByEmail(newEmail).ifPresent(existingUser -> {
                throw new DuplicateEmailException(newEmail);
            });
        }

        user.setName(newName);
        user.setRole(newRole);
        user.setEmail(newEmail);
        user.setProfileImg(newProfileImageUrl);
        user.setPhone(newPhone);

        // @Transactional 어노테이션 덕분에 명시적인 save() 호출 없이도 변경된 필드가 DB에 반영됩니다.
        return user;
    }

    /**
     * 리프레시 토큰(혹은 액세스 토큰)을 무효화하여 사용자를 로그아웃 처리합니다.
     * @param token 무효화할 토큰 (보통 클라이언트에서 전달받은 리프레시 토큰)
     */
    public void logoutUser(String token) {
        tokenServiceImpl.invalidateToken(token);
    }
}