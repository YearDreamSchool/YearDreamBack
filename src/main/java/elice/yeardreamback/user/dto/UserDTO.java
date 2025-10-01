package elice.yeardreamback.user.dto;

import elice.yeardreamback.user.entity.User;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserDTO {

    /**
     * 사용자의 권한 정보입니다. (예: "COACH", "ADMIN", "USER")
     */
    public String role;

    /**
     * 사용자에게 표시되는 이름 또는 닉네임입니다.
     */
    public String name;

    /**
     * 사용자를 고유하게 식별하는 ID 값입니다. (로그인 ID로 사용될 수 있음)
     */
    public String username;

    /**
     * 사용자의 이메일 주소입니다.
     */
    public String email;

    /**
     * 사용자의 프로필 이미지 URL 또는 파일 경로입니다.
     */
    public String profileImg;

    /**
     * 사용자의 전화번호입니다. (예: "010-1234-5678")
     */
    public String phone;
}