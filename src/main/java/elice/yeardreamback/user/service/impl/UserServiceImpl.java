package elice.yeardreamback.user.service.impl;

import elice.yeardreamback.user.entity.User;
import elice.yeardreamback.user.exception.UserNotFoundException;
import elice.yeardreamback.user.repository.UserRepository;
import elice.yeardreamback.oauth.service.impl.TokenServiceImpl;
import elice.yeardreamback.user.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final TokenServiceImpl tokenServiceImpl;
    private final UserRepository userRepository;

    public UserServiceImpl(TokenServiceImpl tokenServiceImpl, UserRepository userRepository) {
        this.tokenServiceImpl = tokenServiceImpl;
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

    public void logoutUser(String token) {
        tokenServiceImpl.invalidateToken(token);
    }
}
