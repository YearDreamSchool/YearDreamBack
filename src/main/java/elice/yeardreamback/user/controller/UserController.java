package elice.yeardreamback.user.controller;

import elice.yeardreamback.oauth.dto.CustomOAuth2User;
import elice.yeardreamback.user.dto.LoginUserResponse;
import elice.yeardreamback.user.dto.UpdateUserRequest;
import elice.yeardreamback.user.entity.User;
import elice.yeardreamback.user.exception.UnauthorizedUserAccessException;
import elice.yeardreamback.user.exception.UserNotAuthenticatedException;
import elice.yeardreamback.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "User", description = "유저 API")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(
            summary = "로그인한 사용자 정보 조회 API",
            description = "현재 로그인한 사용자의 정보를 반환합니다. 현재 발급된 토큰을 조회하여 사용자 정보를 확인합니다. 따라서 Authorization 헤더에 유효한 액세스 토큰이 포함되어 있어야 합니다.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "성공적으로 로그인한 사용자 정보를 조회하였습니다.",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = LoginUserResponse.class))),
                    @ApiResponse(
                            responseCode = "401",
                            description = "사용자가 인증되지 않았습니다.",
                            content = @Content(
                                    mediaType = "application/json", schema = @Schema(implementation = LoginUserResponse.class)))})
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

    @Operation(
            summary = "특정 사용자 정보 조회 API",
            description = "주어진 username에 해당하는 사용자의 정보를 반환합니다.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "성공적으로 특정 사용자 정보를 조회하였습니다.",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = User.class))),
                    @ApiResponse(
                            responseCode = "404",
                            description = "해당 username을 가진 사용자를 찾을 수 없습니다.",
                            content = @Content(
                                    mediaType = "application/json", schema = @Schema(implementation = User.class)))})
    @GetMapping("/{username}")
    public Optional<User> findUserByUsername(@PathVariable String username) {
        return userService.findUserByUsername(username);
    }

    @Operation(
            summary = "특정 사용자 정보 수정 API",
            description = "주어진 username에 해당하는 사용자의 정보를 수정하고, 수정된 사용자 정보를 반환합니다.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "성공적으로 특정 사용자 정보를 수정하였습니다.",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = User.class))),
                    @ApiResponse(
                            responseCode = "404",
                            description = "해당 username을 가진 사용자를 찾을 수 없습니다.",
                            content = @Content(
                                    mediaType = "application/json", schema = @Schema(implementation = User.class)))})
    @PatchMapping("/{username}")
    public User updateUser(@PathVariable String username, @RequestBody UpdateUserRequest updateUserRequest) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UserNotAuthenticatedException("수정 권한을 확인하기 위해 인증이 필요합니다.");
        }
        String currentUsername = authentication.getName();
        if (!currentUsername.equals(username)) {
            throw new UnauthorizedUserAccessException("다른 사용자의 정보를 수정할 수 없습니다.");
        }

        return userService.updateUser(
                username,
                updateUserRequest.getName(),
                updateUserRequest.getRole(),
                updateUserRequest.getEmail(),
                updateUserRequest.getProfileImg(),
                updateUserRequest.getPhone()
        );
    }

    @Operation(
            summary = "로그아웃 API",
            description = "사용자의 리프레시 토큰을 무효화하고, 클라이언트의 쿠키에서 리프레시 토큰을 삭제합니다. Authorization 헤더에 유효한 액세스 토큰이 포함되어 있어야 합니다.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "성공적으로 로그아웃이 처리되었습니다.",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = String.class))),
                    @ApiResponse(
                            responseCode = "400",
                            description = "리프레시 토큰이 없습니다.",
                            content = @Content(
                                    mediaType = "application/json", schema = @Schema(implementation = String.class)))})
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

