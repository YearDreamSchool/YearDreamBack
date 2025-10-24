package elice.yeardreamback.user.service;

import elice.yeardreamback.oauth.service.impl.TokenServiceImpl;
import elice.yeardreamback.user.entity.User;
import elice.yeardreamback.user.exception.DuplicateEmailException;
import elice.yeardreamback.user.exception.UserNotFoundException;
import elice.yeardreamback.user.mapper.UserMapper;
import elice.yeardreamback.user.repository.UserRepository;
import elice.yeardreamback.user.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

// JUnit 5와 Mockito를 통합하여 사용합니다.
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    // 테스트 대상인 Service 구현체에 Mock 객체들을 주입합니다.
    @InjectMocks
    private UserServiceImpl userServiceImpl;

    // Service가 의존하는 Repository와 다른 Service들을 Mocking합니다.
    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenServiceImpl tokenServiceImpl;

    @Mock
    private UserMapper userMapper;

    private User existingUser;
    private final String TEST_USERNAME = "google_user123";

    @BeforeEach
    void setUp() {
        // 모든 테스트에서 사용될 기본 사용자 엔티티를 설정합니다.
        existingUser = new User();
        existingUser.setId(1L);
        existingUser.setUsername(TEST_USERNAME);
        existingUser.setName("엘리스");
        existingUser.setRole("ROLE");
        existingUser.setEmail("test@gmail.com");
        existingUser.setPhone("010-1234-5678");
        existingUser.setProfileImg(null);
    }

    // --- updateUser 메서드 테스트 ---

    @Test
    @DisplayName("사용자 정보 업데이트 성공 - 모든 필드 수정")
    void updateUser_Success_AllFields() {
        // Given
        String newEmail = "new@gmail.com";

        // 1. 기존 사용자가 존재함을 Mocking
        when(userRepository.findByUsername(eq(existingUser.getUsername()))).thenReturn(Optional.of(existingUser));
        // 2. 새 이메일이 중복되지 않음을 Mocking
        when(userRepository.findByEmail(eq(newEmail))).thenReturn(Optional.empty());

        // When
        User updatedUser = userServiceImpl.updateUser(
                existingUser.getUsername(),
                "새로운 이름",
                "COACH",
                newEmail,
                "new_img.jpg",
                "010-9999-8888"
        );

        // Then
        // 1. 반환된 엔티티의 필드 검증
        assertNotNull(updatedUser);
        assertEquals("새로운 이름", updatedUser.getName());
        assertEquals("COACH", updatedUser.getRole());
        assertEquals(newEmail, updatedUser.getEmail());
        assertEquals("new_img.jpg", updatedUser.getProfileImg());
        assertEquals("010-9999-8888", updatedUser.getPhone());

        // 2. findByUsername만 호출되었는지 검증 (findByEmail은 새 이메일로 호출됨)
        verify(userRepository, times(1)).findByUsername(eq(existingUser.getUsername()));
        verify(userRepository, times(1)).findByEmail(eq(newEmail));

        // 3. @Transactional 환경이므로 save()는 명시적으로 호출되지 않음을 확인 (JPA Dirty Checking 가정)
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("사용자 정보 업데이트 성공 - 이메일 변경 없음")
    void updateUser_Success_EmailNotChanged() {
        // Given
        String unchangedEmail = existingUser.getEmail(); // "old@example.com"

        // 1. 기존 사용자가 존재함을 Mocking
        when(userRepository.findByUsername(eq(existingUser.getUsername()))).thenReturn(Optional.of(existingUser));
        // 2. 이메일이 변경되지 않았으므로 findByEmail은 호출되지 않아야 합니다.

        // When
        User updatedUser = userServiceImpl.updateUser(
                existingUser.getUsername(),
                "이름만 변경",
                existingUser.getRole(),
                unchangedEmail, // 기존 이메일 사용
                existingUser.getProfileImg(),
                existingUser.getPhone()
        );

        // Then
        assertEquals("이름만 변경", updatedUser.getName());
        assertEquals(unchangedEmail, updatedUser.getEmail());

        // 1. findByEmail이 호출되지 않았는지 검증
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    @DisplayName("업데이트 실패 - 존재하지 않는 사용자 (UserNotFoundException)")
    void updateUser_Fail_UserNotFound() {
        // Given
        String nonExistentUsername = "non_existent";
        when(userRepository.findByUsername(eq(nonExistentUsername))).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, () -> {
            userServiceImpl.updateUser(
                    nonExistentUsername,
                    "Name", "USER", "email@test.com", "img", "phone"
            );
        });

        // verify(userRepository, never()).findByEmail(anyString()); // 이미 UserNotFoundException 발생으로 findByEmail은 실행되지 않음
    }

    @Test
    @DisplayName("업데이트 실패 - 이메일 중복 (DuplicateEmailException)")
    void updateUser_Fail_DuplicateEmail() {
        // Given
        String duplicateEmail = "duplicate@example.com";
        User otherUser = new User();
        otherUser.setUsername("other_user");
        otherUser.setEmail(duplicateEmail);

        // 1. 기존 사용자가 존재함을 Mocking
        when(userRepository.findByUsername(eq(existingUser.getUsername()))).thenReturn(Optional.of(existingUser));
        // 2. 새 이메일이 이미 존재함을 Mocking
        when(userRepository.findByEmail(eq(duplicateEmail))).thenReturn(Optional.of(otherUser));

        // When & Then
        assertThrows(DuplicateEmailException.class, () -> {
            userServiceImpl.updateUser(
                    existingUser.getUsername(),
                    existingUser.getName(),
                    existingUser.getRole(),
                    duplicateEmail, // 중복 이메일 사용
                    existingUser.getProfileImg(),
                    existingUser.getPhone()
            );
        });
    }

    @Test
    @DisplayName("업데이트 실패 - 이름이 null일 경우 (IllegalArgumentException)")
    void updateUser_Fail_NameIsNull() {
        // Given
        when(userRepository.findByUsername(eq(existingUser.getUsername()))).thenReturn(Optional.of(existingUser));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            userServiceImpl.updateUser(
                    existingUser.getUsername(),
                    null, // 이름 null
                    existingUser.getRole(),
                    "valid@email.com",
                    "img", "phone"
            );
        });
    }

    // --- logoutUser 메서드 테스트 ---

    @Test
    @DisplayName("로그아웃 성공 - 토큰 무효화 서비스 호출 확인")
    void logoutUser_Success() {
        // Given
        String testToken = "mock_refresh_token_string";

        // When
        userServiceImpl.logoutUser(testToken);

        // Then
        // tokenServiceImpl.invalidateToken 메서드가 정확히 1회 호출되었는지 검증
        verify(tokenServiceImpl, times(1)).invalidateToken(eq(testToken));
    }
}