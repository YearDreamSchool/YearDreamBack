package elice.yeardreamback.calender.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 캘린더 이벤트 생성/수정 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class CalendarEventRequest {

    /**
     * 이벤트 제목
     */
    @NotBlank(message = "이벤트 제목은 필수입니다")
    @Size(max = 100, message = "이벤트 제목은 100자를 초과할 수 없습니다")
    private String title;

    /**
     * 이벤트 설명
     */
    @Size(max = 500, message = "이벤트 설명은 500자를 초과할 수 없습니다")
    private String description;

    /**
     * 이벤트 시작 시간
     */
    @NotNull(message = "이벤트 시작 시간은 필수입니다")
    private LocalDateTime startTime;

    /**
     * 이벤트 종료 시간
     */
    @NotNull(message = "이벤트 종료 시간은 필수입니다")
    private LocalDateTime endTime;

    /**
     * 이벤트 장소
     */
    @Size(max = 200, message = "이벤트 장소는 200자를 초과할 수 없습니다")
    private String location;

    /**
     * 카테고리 ID
     */
    private Long categoryId;

    /**
     * 알림 설정 (분 단위)
     * 예: [30, 60] = 30분 전, 1시간 전 알림
     */
    private List<@Min(value = 0, message = "알림 시간은 0분 이상이어야 합니다") Integer> reminderMinutes;

    /**
     * 생성자
     */
    public CalendarEventRequest(String title, String description, LocalDateTime startTime, 
                               LocalDateTime endTime, String location, Long categoryId, 
                               List<Integer> reminderMinutes) {
        this.title = title;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.categoryId = categoryId;
        this.reminderMinutes = reminderMinutes;
    }

    /**
     * 시간 범위 유효성 검사
     */
    public boolean isValidTimeRange() {
        return startTime != null && endTime != null && startTime.isBefore(endTime);
    }
}