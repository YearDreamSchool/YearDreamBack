package elice.yeardreamback.oauth.service;

import elice.yeardreamback.oauth.dto.*;
import elice.yeardreamback.user.dto.UserDTO;
import elice.yeardreamback.user.entity.User;
import elice.yeardreamback.user.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);
        System.out.println(oAuth2User);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Response oAuth2Response;

        if (registrationId.equals("naver")) {
            oAuth2Response = new NaverResponse(oAuth2User.getAttributes());
        } else if (registrationId.equals("google")) {
            oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());
        } else if (registrationId.equals("kakao")) {
            oAuth2Response = new KakaoResponse(oAuth2User.getAttributes());
        } else {
            return null;
        }

        String username = oAuth2Response.getProvider() + " " + oAuth2Response.getProviderId();

        User user = userRepository.findByUsername(username)
                .map(existingUser -> {
                    existingUser.setName(oAuth2Response.getName());
                    existingUser.setEmail(oAuth2Response.getEmail());
                    return userRepository.save(existingUser);
                })
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setName(oAuth2Response.getName());
                    newUser.setUsername(username);
                    newUser.setEmail(oAuth2Response.getEmail());
                    newUser.setRole("USER");
                    return userRepository.save(newUser);
                });

        UserDTO userDTO = UserDTO.builder()
                .username(user.getUsername())
                .name(user.getName())
                .role(user.getRole())
                .build();

        return new CustomOAuth2User(userDTO);
    }
}
