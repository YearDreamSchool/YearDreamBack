package elice.yeardreamback.calender.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 이벤트 알림 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class EventReminderResponse {

    /**
     * 알림 ID
     */
    private Long id;

    /**
     * 이벤트 시작 전 몇 분 전에 알림을 보낼지
     */
    private Integer minutesBefore;

    /**
     * 알림 활성화 여부
     */
    private Boolean isActive;

    /**
     * 알림이 발송될 시간
     */
    private LocalDateTime reminderTime;

    /**
     * 알림이 미래인지 여부 (아직 발송되지 않은 알림)
     */
    private boolean isUpcoming;

    /**
     * 생성 시간
     */
    private LocalDateTime createdAt;

    /**
     * 생성자
     */
    public EventReminderResponse(Long id, Integer minutesBefore, Boolean isActive,
                                LocalDateTime reminderTime, boolean isUpcoming,
                                LocalDateTime createdAt) {
        this.id = id;
        this.minutesBefore = minutesBefore;
        this.isActive = isActive;
        this.reminderTime = reminderTime;
        this.isUpcoming = isUpcoming;
        this.createdAt = createdAt;
    }

    /**
     * 알림 시간을 사용자 친화적 문자열로 변환
     */
    public String getDisplayText() {
        if (minutesBefore == null) {
            return "알림 없음";
        }
        
        if (minutesBefore == 0) {
            return "이벤트 시작 시";
        } else if (minutesBefore < 60) {
            return minutesBefore + "분 전";
        } else if (minutesBefore < 1440) { // 24시간 미만
            int hours = minutesBefore / 60;
            int remainingMinutes = minutesBefore % 60;
            if (remainingMinutes == 0) {
                return hours + "시간 전";
            } else {
                return hours + "시간 " + remainingMinutes + "분 전";
            }
        } else { // 1일 이상
            int days = minutesBefore / 1440;
            int remainingHours = (minutesBefore % 1440) / 60;
            if (remainingHours == 0) {
                return days + "일 전";
            } else {
                return days + "일 " + remainingHours + "시간 전";
            }
        }
    }
}