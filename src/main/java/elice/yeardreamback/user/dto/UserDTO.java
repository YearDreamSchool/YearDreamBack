package elice.yeardreamback.user.dto;

import elice.yeardreamback.user.entity.User;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserDTO {

    public String role;
    public String name;
    public String username;
    public String email;
    public String profileImg;
    public String phone;

    public static UserDTO fromEntity(User user) {
        return UserDTO.builder()
                .role(user.getRole())
                .name(user.getName())
                .username(user.getUsername())
                .email(user.getEmail())
                .profileImg(user.getProfileImg())
                .phone(user.getPhone())
                .build();
    }
}
