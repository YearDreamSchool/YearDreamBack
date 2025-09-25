package elice.yeardreamback.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRequest {
    private String username;
    private String name;
    private String role;
    private String email;
    private String profileImg;
    private String phone;
}
