package elice.yeardreamback.calender.dto;

import elice.yeardreamback.calender.enums.SharePermission;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 이벤트 공유 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class EventShareRequest {

    /**
     * 공유받을 사용자의 사용자명
     */
    @NotBlank(message = "공유받을 사용자명은 필수입니다")
    private String sharedWithUsername;

    /**
     * 공유 권한
     */
    @NotNull(message = "공유 권한은 필수입니다")
    private SharePermission permission;

    /**
     * 생성자
     */
    public EventShareRequest(String sharedWithUsername, SharePermission permission) {
        this.sharedWithUsername = sharedWithUsername;
        this.permission = permission;
    }
}