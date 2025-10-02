package elice.yeardreamback.user.controller;

import elice.yeardreamback.oauth.dto.CustomOAuth2User;
import elice.yeardreamback.user.dto.UserDTO;
import elice.yeardreamback.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    @DisplayName("로그인 된 유저 정보 가져오기 - CustomOAuth2User를 Principal로 사용")
    void getLoginedUserTest_WithCustomOAuth2User() throws Exception {

        // given
        String mockUsername = "google_1012345678";
        String mockName = "테스트 유저";
        String mockRole = "USER";
        String registrationId = "google";

        UserDTO mockUserDTO = UserDTO.builder().username(mockUsername).name(mockName).role(mockRole).build();
        CustomOAuth2User mockPrincipal = new CustomOAuth2User(mockUserDTO);

        OAuth2AuthenticationToken mockAuthentication = new OAuth2AuthenticationToken(
                mockPrincipal,
                mockPrincipal.getAuthorities(),
                registrationId
        );

        // when & then
        mockMvc.perform(
                        get("/api/users/logined")
                                .with(authentication(mockAuthentication))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(mockUsername))
                .andExpect(jsonPath("$.name").value(mockName))
                .andExpect(jsonPath("$.role").value(mockRole));

    }

    @Test
    @DisplayName("로그인 되지 않은 상태에서 유저 정보 요청 - 302 Found 예상")
    void getLoginedUserTest_Failure_Unauthenticated() throws Exception {
        mockMvc.perform(
                        get("/api/users/logined")
                )
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("http://localhost/login"));
    }
}