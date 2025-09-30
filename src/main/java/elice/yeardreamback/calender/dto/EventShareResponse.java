package elice.yeardreamback.calender.dto;

import elice.yeardreamback.calender.enums.SharePermission;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 이벤트 공유 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class EventShareResponse {

    /**
     * 공유 ID
     */
    private Long id;

    /**
     * 이벤트 ID
     */
    private Long eventId;

    /**
     * 이벤트 제목
     */
    private String eventTitle;

    /**
     * 공유받은 사용자 정보
     */
    private String sharedWithUsername;

    /**
     * 공유받은 사용자 이름
     */
    private String sharedWithName;

    /**
     * 공유 권한
     */
    private SharePermission permission;

    /**
     * 이벤트 소유자 사용자명
     */
    private String eventOwnerUsername;

    /**
     * 이벤트 소유자 이름
     */
    private String eventOwnerName;

    /**
     * 공유 시간
     */
    private LocalDateTime sharedAt;

    /**
     * 생성자
     */
    public EventShareResponse(Long id, Long eventId, String eventTitle,
                             String sharedWithUsername, String sharedWithName,
                             SharePermission permission,
                             String eventOwnerUsername, String eventOwnerName,
                             LocalDateTime sharedAt) {
        this.id = id;
        this.eventId = eventId;
        this.eventTitle = eventTitle;
        this.sharedWithUsername = sharedWithUsername;
        this.sharedWithName = sharedWithName;
        this.permission = permission;
        this.eventOwnerUsername = eventOwnerUsername;
        this.eventOwnerName = eventOwnerName;
        this.sharedAt = sharedAt;
    }

    /**
     * 편집 권한이 있는지 확인
     */
    public boolean canEdit() {
        return permission == SharePermission.EDIT;
    }

    /**
     * 읽기 권한이 있는지 확인 (모든 공유는 기본적으로 읽기 권한 포함)
     */
    public boolean canView() {
        return true;
    }

    /**
     * 권한을 사용자 친화적 문자열로 변환
     */
    public String getPermissionDisplayText() {
        return permission != null ? permission.getDescription() : "권한 없음";
    }
}