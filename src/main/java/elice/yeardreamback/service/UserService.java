package elice.yeardreamback.service;

import elice.yeardreamback.entity.User;
import elice.yeardreamback.exception.UserNotFoundException;
import elice.yeardreamback.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional
    public User updateUser(String username, String newName, String newRole, String newEmail, String newProfileImageUrl, String newPhone) {
        User user = userRepository.findByUsername(username)
                        .orElseThrow(() -> new UserNotFoundException(username));

        user.setName(newName);
        user.setRole(newRole);
        user.setEmail(newEmail);
        user.setProfileImg(newProfileImageUrl);
        user.setPhone(newPhone);

        return user;
    }
}
