package elice.yeardreamback.calender.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 이벤트 알림 엔티티
 * 이벤트에 대한 알림 설정 정보를 저장
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "event_reminders")
@EntityListeners(AuditingEntityListener.class)
public class EventReminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 알림이 설정된 이벤트
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private CalendarEvent event;

    /**
     * 이벤트 시작 전 몇 분 전에 알림을 보낼지 설정
     */
    @NotNull(message = "알림 시간은 필수입니다")
    @Min(value = 0, message = "알림 시간은 0분 이상이어야 합니다")
    @Column(name = "minutes_before", nullable = false)
    private Integer minutesBefore;

    /**
     * 알림 활성화 여부
     */
    @NotNull(message = "알림 활성화 여부는 필수입니다")
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * 생성자
     */
    public EventReminder(CalendarEvent event, Integer minutesBefore) {
        this.event = event;
        this.minutesBefore = minutesBefore;
        this.isActive = true;
    }

    /**
     * 알림 시간 계산
     * @return 알림이 발송될 시간
     */
    public LocalDateTime getReminderTime() {
        if (event != null && event.getStartTime() != null) {
            return event.getStartTime().minusMinutes(minutesBefore);
        }
        return null;
    }

    /**
     * 알림 유효성 검사
     */
    public void validateReminder() {
        if (event == null) {
            throw new IllegalArgumentException("알림이 설정될 이벤트는 필수입니다");
        }
        
        if (minutesBefore == null) {
            throw new IllegalArgumentException("알림 시간은 필수입니다");
        }
        
        if (minutesBefore < 0) {
            throw new IllegalArgumentException("알림 시간은 0분 이상이어야 합니다");
        }
        
        // 최대 1주일 전까지만 알림 설정 가능
        if (minutesBefore > 10080) { // 7일 * 24시간 * 60분
            throw new IllegalArgumentException("알림은 최대 1주일 전까지만 설정할 수 있습니다");
        }
    }

    /**
     * 알림이 현재 시간 이후인지 확인
     */
    public boolean isUpcoming() {
        LocalDateTime reminderTime = getReminderTime();
        return reminderTime != null && reminderTime.isAfter(LocalDateTime.now());
    }

    /**
     * 알림 활성화
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * 알림 비활성화
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * 저장 전 유효성 검사
     */
    @PrePersist
    @PreUpdate
    private void validateBeforeSave() {
        validateReminder();
    }
}