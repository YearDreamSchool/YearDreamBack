package elice.yeardreamback.calender.entity;

import elice.yeardreamback.calender.enums.SharePermission;
import elice.yeardreamback.calender.exception.EventSharingException;
import elice.yeardreamback.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 이벤트 공유 엔티티
 * 이벤트를 다른 사용자와 공유하는 정보를 저장
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "event_shares", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"event_id", "shared_with_user_id"}))
@EntityListeners(AuditingEntityListener.class)
public class EventShare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 공유된 이벤트
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private CalendarEvent event;

    /**
     * 이벤트를 공유받은 사용자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_with_user_id", nullable = false)
    private User sharedWithUser;

    /**
     * 공유 권한 (읽기 전용 또는 편집 가능)
     */
    @NotNull(message = "공유 권한은 필수입니다")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SharePermission permission;

    @CreatedDate
    @Column(name = "shared_at", updatable = false)
    private LocalDateTime sharedAt;

    /**
     * 생성자
     */
    public EventShare(CalendarEvent event, User sharedWithUser, SharePermission permission) {
        this.event = event;
        this.sharedWithUser = sharedWithUser;
        this.permission = permission;
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
     * 공유 유효성 검사
     */
    public void validateShare() {
        if (event == null) {
            throw new IllegalArgumentException("공유할 이벤트는 필수입니다");
        }
        
        if (sharedWithUser == null) {
            throw new IllegalArgumentException("공유받을 사용자는 필수입니다");
        }
        
        if (permission == null) {
            throw new IllegalArgumentException("공유 권한은 필수입니다");
        }
        
        // 자기 자신과는 공유할 수 없음
        if (event.getUser() != null && event.getUser().equals(sharedWithUser)) {
            throw new EventSharingException("자기 자신과는 이벤트를 공유할 수 없습니다");
        }
    }

    /**
     * 권한 변경
     */
    public void changePermission(SharePermission newPermission) {
        if (newPermission == null) {
            throw new IllegalArgumentException("새로운 권한은 필수입니다");
        }
        this.permission = newPermission;
    }

    /**
     * 공유 소유자인지 확인 (이벤트 소유자만 공유 설정 변경 가능)
     */
    public boolean isOwnedBy(User user) {
        return event != null && event.getUser() != null && event.getUser().equals(user);
    }

    /**
     * 공유받은 사용자인지 확인
     */
    public boolean isSharedWith(User user) {
        return sharedWithUser != null && sharedWithUser.equals(user);
    }

    /**
     * 저장 전 유효성 검사
     */
    @PrePersist
    @PreUpdate
    private void validateBeforeSave() {
        validateShare();
    }
}