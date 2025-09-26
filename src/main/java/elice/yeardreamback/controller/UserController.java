package elice.yeardreamback.controller;

import elice.yeardreamback.dto.CustomOAuth2User;
import elice.yeardreamback.dto.LoginUserResponse;
import elice.yeardreamback.dto.LogoutRequest;
import elice.yeardreamback.dto.UpdateUserRequest;
import elice.yeardreamback.entity.User;
import elice.yeardreamback.exception.UserNotAuthenticatedException;
import elice.yeardreamback.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
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

    @PatchMapping("/{username}")
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

    @PostMapping("/logout")
    public ResponseEntity<String> logoutUser(HttpServletRequest request, HttpServletResponse response, @RequestHeader("Authorization") String authorizationHeader) {
        // 쿠키에서 refresh token 가져오기
        Cookie[] cookies = request.getCookies();
        String refreshToken = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        if (refreshToken == null) {
            return ResponseEntity.badRequest().body("리프레시 토큰이 없습니다.");
        }

        userService.logoutUser(refreshToken);

        Cookie deleteCookie = new Cookie("refreshToken", null);
        deleteCookie.setHttpOnly(true);
        deleteCookie.setSecure(false);
        deleteCookie.setPath("/");
        deleteCookie.setMaxAge(0);
        response.addCookie(deleteCookie);

        return ResponseEntity.ok("로그아웃이 성공적으로 처리되었습니다.");
    }
}

