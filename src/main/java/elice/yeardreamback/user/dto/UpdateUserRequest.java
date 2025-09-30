package elice.yeardreamback.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UpdateUserRequest {
    /**
     * 수정 대상 사용자의 고유 ID (username).
     * 보통 URL Path Variable로 전달되지만, DTO에 포함될 수도 있습니다.
     */
    private String username;

    /**
     * 수정할 사용자 이름 또는 닉네임입니다.
     */
    private String name;

    /**
     * 수정할 사용자의 권한 (Role) 정보입니다. (예: "USER", "COACH", ADMIN)
     */
    private String role;

    /**
     * 수정할 사용자의 이메일 주소입니다.
     */
    private String email;

    /**
     * 수정할 프로필 이미지 URL 또는 파일 경로입니다.
     */
    private String profileImg;

    /**
     * 수정할 사용자의 전화번호입니다. (예: "010-1234-5678")
     */
    private String phone;
}