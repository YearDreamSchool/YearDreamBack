package elice.yeardreamback.calender.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 이벤트 카테고리 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class EventCategoryResponse {

    /**
     * 카테고리 ID
     */
    private Long id;

    /**
     * 카테고리 이름
     */
    private String name;

    /**
     * 카테고리 색상 (HEX 코드)
     */
    private String color;

    /**
     * 카테고리 설명
     */
    private String description;

    /**
     * 이 카테고리에 속한 이벤트 개수
     */
    private int eventCount;

    /**
     * 삭제 가능 여부 (이벤트가 없는 경우에만 삭제 가능)
     */
    private boolean canBeDeleted;

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
    public EventCategoryResponse(Long id, String name, String color, String description,
                                int eventCount, boolean canBeDeleted,
                                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.description = description;
        this.eventCount = eventCount;
        this.canBeDeleted = canBeDeleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}