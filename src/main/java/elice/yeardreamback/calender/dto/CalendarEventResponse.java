package elice.yeardreamback.calender.dto;

import elice.yeardreamback.calender.enums.EventStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 캘린더 이벤트 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class CalendarEventResponse {

    /**
     * 이벤트 ID
     */
    private Long id;

    /**
     * 이벤트 제목
     */
    private String title;

    /**
     * 이벤트 설명
     */
    private String description;

    /**
     * 이벤트 시작 시간
     */
    private LocalDateTime startTime;

    /**
     * 이벤트 종료 시간
     */
    private LocalDateTime endTime;

    /**
     * 이벤트 장소
     */
    private String location;

    /**
     * 이벤트 상태
     */
    private EventStatus status;

    /**
     * 이벤트 카테고리 정보
     */
    private EventCategoryResponse category;

    /**
     * 이벤트 알림 목록
     */
    private List<EventReminderResponse> reminders;

    /**
     * 이벤트 소유자 정보
     */
    private String ownerUsername;

    /**
     * 공유 여부 (현재 사용자가 소유자가 아닌 경우)
     */
    private boolean isShared;

    /**
     * 편집 권한 여부 (공유된 이벤트인 경우)
     */
    private boolean canEdit;

    /**
     * 생성 시간
     */
    private LocalDateTime createdAt;

    /**
     * 수정 시간
     */
    private LocalDateTime updatedAt;

    /**
     * 생성자
     */
    public CalendarEventResponse(Long id, String title, String description, 
                                LocalDateTime startTime, LocalDateTime endTime, 
                                String location, EventStatus status, 
                                EventCategoryResponse category, 
                                List<EventReminderResponse> reminders,
                                String ownerUsername, boolean isShared, boolean canEdit,
                                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.status = status;
        this.category = category;
        this.reminders = reminders;
        this.ownerUsername = ownerUsername;
        this.isShared = isShared;
        this.canEdit = canEdit;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 이벤트 기간 계산 (분 단위)
     */
    public long getDurationInMinutes() {
        if (startTime != null && endTime != null) {
            return java.time.Duration.between(startTime, endTime).toMinutes();
        }
        return 0;
    }

    /**
     * 이벤트가 진행 중인지 확인
     */
    public boolean isOngoing() {
        LocalDateTime now = LocalDateTime.now();
        return startTime != null && endTime != null && 
               now.isAfter(startTime) && now.isBefore(endTime);
    }

    /**
     * 이벤트가 완료되었는지 확인
     */
    public boolean isPast() {
        return endTime != null && endTime.isBefore(LocalDateTime.now());
    }

    /**
     * 이벤트가 미래인지 확인
     */
    public boolean isFuture() {
        return startTime != null && startTime.isAfter(LocalDateTime.now());
    }
}