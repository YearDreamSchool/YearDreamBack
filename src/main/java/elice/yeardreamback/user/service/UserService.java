package elice.yeardreamback.user.service;

import elice.yeardreamback.user.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface UserService {

    Optional<User> findUserByUsername(String username);

    @Transactional
    User updateUser(String username, String newName, String newRole, String newEmail, String newProfileImageUrl, String newPhone);

    void logoutUser(String token);
}
