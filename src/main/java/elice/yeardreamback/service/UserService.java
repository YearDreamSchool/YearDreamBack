package elice.yeardreamback.service;

import elice.yeardreamback.entity.User;
import elice.yeardreamback.exception.UserNotFoundException;
import elice.yeardreamback.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface UserService {

    Optional<User> findUserByUsername(String username);

    @Transactional
    User updateUser(String username, String newName, String newRole, String newEmail, String newProfileImageUrl, String newPhone);

    Optional<User> logoutUser(String username);
}
