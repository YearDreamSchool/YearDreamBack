package elice.yeardreamback.service;

import elice.yeardreamback.entity.User;
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
