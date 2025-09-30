package elice.yeardreamback.calender.entity;

import elice.yeardreamback.calender.enums.EventStatus;
import elice.yeardreamback.calender.exception.InvalidEventTimeException;
import elice.yeardreamback.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 캘린더 이벤트 엔티티
 * 사용자의 일정 정보를 저장하는 핵심 엔티티
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "calendar_events")
@EntityListeners(AuditingEntityListener.class)
public class CalendarEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 이벤트를 소유한 사용자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 이벤트 제목
     */
    @NotBlank(message = "이벤트 제목은 필수입니다")
    @Size(max = 100, message = "이벤트 제목은 100자를 초과할 수 없습니다")
    @Column(nullable = false, length = 100)
    private String title;

    /**
     * 이벤트 설명
     */
    @Size(max = 500, message = "이벤트 설명은 500자를 초과할 수 없습니다")
    @Column(length = 500)
    private String description;

    /**
     * 이벤트 시작 시간
     */
    @NotNull(message = "이벤트 시작 시간은 필수입니다")
    @Future(message = "이벤트 시작 시간은 현재 시간 이후여야 합니다")
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    /**
     * 이벤트 종료 시간
     */
    @NotNull(message = "이벤트 종료 시간은 필수입니다")
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    /**
     * 이벤트 장소
     */
    @Size(max = 200, message = "이벤트 장소는 200자를 초과할 수 없습니다")
    @Column(length = 200)
    private String location;

    /**
     * 이벤트 상태
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status = EventStatus.SCHEDULED;

    /**
     * 이벤트 카테고리
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private EventCategory category;

    /**
     * 이벤트 알림 목록
     */
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EventReminder> reminders = new ArrayList<>();

    /**
     * 이벤트 공유 목록
     */
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EventShare> shares = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 생성자
     */
    public CalendarEvent(User user, String title, String description, 
                        LocalDateTime startTime, LocalDateTime endTime, 
                        String location, EventCategory category) {
        this.user = user;
        this.title = title;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.category = category;
        this.status = EventStatus.SCHEDULED;
    }

    /**
     * 이벤트 시간 유효성 검사
     * @return 시작 시간이 종료 시간보다 이전인지 여부
     */
    public boolean isValidTimeRange() {
        return startTime != null && endTime != null && startTime.isBefore(endTime);
    }

    /**
     * 이벤트 데이터 전체 유효성 검사
     * @throws InvalidEventTimeException 시간 범위가 유효하지 않을 때
     * @throws IllegalArgumentException 필수 데이터가 누락되었을 때
     */
    public void validateEvent() {
        // 필수 필드 검사
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("이벤트 제목은 필수입니다");
        }
        
        if (startTime == null) {
            throw new IllegalArgumentException("이벤트 시작 시간은 필수입니다");
        }
        
        if (endTime == null) {
            throw new IllegalArgumentException("이벤트 종료 시간은 필수입니다");
        }
        
        if (user == null) {
            throw new IllegalArgumentException("이벤트 소유자는 필수입니다");
        }

        // 시간 범위 검사
        if (!isValidTimeRange()) {
            throw new InvalidEventTimeException("이벤트 시작 시간은 종료 시간보다 이전이어야 합니다");
        }

        // 이벤트 기간이 너무 긴지 검사 (최대 7일)
        if (startTime.plusDays(7).isBefore(endTime)) {
            throw new InvalidEventTimeException("이벤트 기간은 최대 7일을 초과할 수 없습니다");
        }
    }

    /**
     * 이벤트 수정 전 유효성 검사
     */
    @PrePersist
    @PreUpdate
    private void validateBeforeSave() {
        validateEvent();
    }

    /**
     * 알림 추가
     */
    public void addReminder(EventReminder reminder) {
        reminders.add(reminder);
        reminder.setEvent(this);
    }

    /**
     * 알림 제거
     */
    public void removeReminder(EventReminder reminder) {
        reminders.remove(reminder);
        reminder.setEvent(null);
    }

    /**
     * 공유 추가
     */
    public void addShare(EventShare share) {
        shares.add(share);
        share.setEvent(this);
    }

    /**
     * 공유 제거
     */
    public void removeShare(EventShare share) {
        shares.remove(share);
        share.setEvent(null);
    }

	public String getOwnerUsername() {
		return this.user.getUsername();
	}
}