package elice.yeardreamback.service.impl;

import elice.yeardreamback.entity.User;
import elice.yeardreamback.exception.UserNotFoundException;
import elice.yeardreamback.repository.UserRepository;
import elice.yeardreamback.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final TokenService tokenService;
    private final UserRepository userRepository;

    public UserServiceImpl(TokenService tokenService, UserRepository userRepository) {
        this.tokenService = tokenService;
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
        tokenService.invalidateToken(token);
    }
}
