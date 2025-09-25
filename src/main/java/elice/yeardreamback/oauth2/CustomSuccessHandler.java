package elice.yeardreamback.oauth2;

import elice.yeardreamback.dto.CustomOAuth2User;
import elice.yeardreamback.jwt.JWTUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

@Component
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;

    public CustomSuccessHandler(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        // OAuth2User
        CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        String username = customOAuth2User.getUsername();
        String name = customOAuth2User.getName();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        long accessExpiredMs = 30 * 1000L;
        long refreshExpiredMs = 7 * 24 * 60 * 60 * 1000L;

        String accessToken = jwtUtil.createJwt("access", username, role, name, accessExpiredMs);
        String refreshToken = jwtUtil.createJwt("refresh", username, role, name, refreshExpiredMs);

        // RefreshToken을 HttpOnly 쿠키로 저장
        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge((int) (refreshExpiredMs / 1000));
        response.addCookie(refreshCookie);

        // 액세스 토큰은 redirect URL에 쿼리로 전달
        String redirectUrl = "http://localhost:3000/oauth/redirect?token=" + accessToken;
        response.sendRedirect(redirectUrl);
    }
}
