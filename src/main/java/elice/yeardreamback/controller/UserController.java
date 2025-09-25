package elice.yeardreamback.controller;

import elice.yeardreamback.dto.CustomOAuth2User;
import elice.yeardreamback.dto.LoginUserResponse;
import elice.yeardreamback.dto.UpdateUserRequest;
import elice.yeardreamback.entity.User;
import elice.yeardreamback.exception.UserNotAuthenticatedException;
import elice.yeardreamback.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/logined")
    public LoginUserResponse getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UserNotAuthenticatedException("사용자가 인증되지 않았습니다.");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomOAuth2User oauth2User) {
            return new LoginUserResponse(
                    oauth2User.getUsername(),
                    oauth2User.getName(),
                    oauth2User.getRole()
            );
        } else {
            throw new UserNotAuthenticatedException("사용자가 인증되지 않았습니다.");
        }
    }

    @GetMapping("/{username}")
    public Optional<User> findUserByUsername(@PathVariable String username) {
        return userService.findUserByUsername(username);
    }

    @PostMapping("/{username}")
    public User updateUser(@PathVariable String username, @RequestBody UpdateUserRequest updateUserRequest) {
        return userService.updateUser(
                username,
                updateUserRequest.getName(),
                updateUserRequest.getRole(),
                updateUserRequest.getEmail(),
                updateUserRequest.getProfileImg(),
                updateUserRequest.getPhone()
        );
    }
}

