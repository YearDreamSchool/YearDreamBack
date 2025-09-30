package elice.yeardreamback.calender.entity;

import elice.yeardreamback.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
 * 이벤트 카테고리 엔티티
 * 사용자가 이벤트를 분류하기 위해 생성하는 카테고리 정보를 저장
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "event_categories")
@EntityListeners(AuditingEntityListener.class)
public class EventCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 카테고리를 소유한 사용자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 카테고리 이름
     */
    @NotBlank(message = "카테고리 이름은 필수입니다")
    @Size(max = 50, message = "카테고리 이름은 50자를 초과할 수 없습니다")
    @Column(nullable = false, length = 50)
    private String name;

    /**
     * 카테고리 색상 (HEX 코드)
     */
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "색상은 유효한 HEX 코드 형식이어야 합니다 (예: #FF0000)")
    @Column(length = 7)
    private String color;

    /**
     * 카테고리 설명
     */
    @Size(max = 200, message = "카테고리 설명은 200자를 초과할 수 없습니다")
    @Column(length = 200)
    private String description;

    /**
     * 이 카테고리에 속한 이벤트들
     */
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CalendarEvent> events = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 생성자
     */
    public EventCategory(User user, String name, String color, String description) {
        this.user = user;
        this.name = name;
        this.color = color;
        this.description = description;
    }

    /**
     * 카테고리 유효성 검사
     */
    public void validateCategory() {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("카테고리 이름은 필수입니다");
        }
        
        if (user == null) {
            throw new IllegalArgumentException("카테고리 소유자는 필수입니다");
        }

        if (color != null && !isValidHexColor(color)) {
            throw new IllegalArgumentException("색상은 유효한 HEX 코드 형식이어야 합니다 (예: #FF0000)");
        }
    }

    /**
     * HEX 색상 코드 유효성 검사
     */
    private boolean isValidHexColor(String color) {
        return color != null && color.matches("^#[0-9A-Fa-f]{6}$");
    }

    /**
     * 기본 색상 설정 (색상이 없는 경우)
     */
    public void setDefaultColorIfNull() {
        if (color == null || color.trim().isEmpty()) {
            this.color = "#3498db"; // 기본 파란색
        }
    }

    /**
     * 카테고리에 속한 이벤트 개수 반환
     */
    public int getEventCount() {
        return events != null ? events.size() : 0;
    }

    /**
     * 카테고리 삭제 가능 여부 확인 (이벤트가 없는 경우에만 삭제 가능)
     */
    public boolean canBeDeleted() {
        return getEventCount() == 0;
    }

    /**
     * 저장 전 유효성 검사 및 기본값 설정
     */
    @PrePersist
    @PreUpdate
    private void validateBeforeSave() {
        validateCategory();
        setDefaultColorIfNull();
    }
}