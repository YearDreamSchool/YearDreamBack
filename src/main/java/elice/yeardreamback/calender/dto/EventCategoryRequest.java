package elice.yeardreamback.calender.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 이벤트 카테고리 생성/수정 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class EventCategoryRequest {

    /**
     * 카테고리 이름
     */
    @NotBlank(message = "카테고리 이름은 필수입니다")
    @Size(max = 50, message = "카테고리 이름은 50자를 초과할 수 없습니다")
    private String name;

    /**
     * 카테고리 색상 (HEX 코드)
     */
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "색상은 유효한 HEX 코드 형식이어야 합니다 (예: #FF0000)")
    private String color;

    /**
     * 카테고리 설명
     */
    @Size(max = 200, message = "카테고리 설명은 200자를 초과할 수 없습니다")
    private String description;

    /**
     * 생성자
     */
    public EventCategoryRequest(String name, String color, String description) {
        this.name = name;
        this.color = color;
        this.description = description;
    }
}