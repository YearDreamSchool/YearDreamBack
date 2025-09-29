package elice.yeardreamback.calender.entity;

import elice.yeardreamback.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * EventCategory 엔티티 테스트
 */
class EventCategoryTest {

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setName("테스트 사용자");
    }

    @Test
    @DisplayName("유효한 카테고리 생성 테스트")
    void createValidCategory() {
        // given & when
        EventCategory category = new EventCategory(
            testUser,
            "업무",
            "#FF0000",
            "업무 관련 일정"
        );

        // then
        assertThatNoException().isThrownBy(category::validateCategory);
        assertThat(category.getName()).isEqualTo("업무");
        assertThat(category.getColor()).isEqualTo("#FF0000");
        assertThat(category.getDescription()).isEqualTo("업무 관련 일정");
        assertThat(category.getUser()).isEqualTo(testUser);
    }

    @Test
    @DisplayName("카테고리 이름이 null인 경우 예외 발생")
    void throwExceptionWhenNameIsNull() {
        // given
        EventCategory category = new EventCategory(
            testUser,
            null,
            "#FF0000",
            "설명"
        );

        // when & then
        assertThatThrownBy(category::validateCategory)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("카테고리 이름은 필수입니다");
    }

    @Test
    @DisplayName("카테고리 이름이 빈 문자열인 경우 예외 발생")
    void throwExceptionWhenNameIsEmpty() {
        // given
        EventCategory category = new EventCategory(
            testUser,
            "   ",
            "#FF0000",
            "설명"
        );

        // when & then
        assertThatThrownBy(category::validateCategory)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("카테고리 이름은 필수입니다");
    }

    @Test
    @DisplayName("사용자가 null인 경우 예외 발생")
    void throwExceptionWhenUserIsNull() {
        // given
        EventCategory category = new EventCategory(
            null,
            "업무",
            "#FF0000",
            "설명"
        );

        // when & then
        assertThatThrownBy(category::validateCategory)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("카테고리 소유자는 필수입니다");
    }

    @Test
    @DisplayName("유효하지 않은 HEX 색상 코드인 경우 예외 발생")
    void throwExceptionWhenInvalidHexColor() {
        // given
        EventCategory category = new EventCategory(
            testUser,
            "업무",
            "invalid-color",
            "설명"
        );

        // when & then
        assertThatThrownBy(category::validateCategory)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("색상은 유효한 HEX 코드 형식이어야 합니다 (예: #FF0000)");
    }

    @Test
    @DisplayName("유효한 HEX 색상 코드 테스트")
    void validHexColorCodes() {
        // given
        String[] validColors = {"#FF0000", "#00FF00", "#0000FF", "#FFFFFF", "#000000", "#123ABC"};

        for (String color : validColors) {
            EventCategory category = new EventCategory(
                testUser,
                "테스트",
                color,
                "설명"
            );

            // when & then
            assertThatNoException().isThrownBy(category::validateCategory);
        }
    }

    @Test
    @DisplayName("유효하지 않은 HEX 색상 코드 테스트")
    void invalidHexColorCodes() {
        // given
        String[] invalidColors = {"FF0000", "#FF00", "#GGGGGG", "red", "#FF00000", ""};

        for (String color : invalidColors) {
            EventCategory category = new EventCategory(
                testUser,
                "테스트",
                color,
                "설명"
            );

            // when & then
            assertThatThrownBy(category::validateCategory)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("색상은 유효한 HEX 코드 형식이어야 합니다 (예: #FF0000)");
        }
    }

    @Test
    @DisplayName("색상이 null인 경우 기본 색상 설정")
    void setDefaultColorWhenNull() {
        // given
        EventCategory category = new EventCategory(
            testUser,
            "업무",
            null,
            "설명"
        );

        // when
        category.setDefaultColorIfNull();

        // then
        assertThat(category.getColor()).isEqualTo("#3498db");
    }

    @Test
    @DisplayName("색상이 빈 문자열인 경우 기본 색상 설정")
    void setDefaultColorWhenEmpty() {
        // given
        EventCategory category = new EventCategory(
            testUser,
            "업무",
            "   ",
            "설명"
        );

        // when
        category.setDefaultColorIfNull();

        // then
        assertThat(category.getColor()).isEqualTo("#3498db");
    }

    @Test
    @DisplayName("이벤트 개수 반환 테스트")
    void getEventCount() {
        // given
        EventCategory category = new EventCategory(
            testUser,
            "업무",
            "#FF0000",
            "설명"
        );

        // when & then
        assertThat(category.getEventCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("이벤트가 없는 경우 삭제 가능")
    void canBeDeletedWhenNoEvents() {
        // given
        EventCategory category = new EventCategory(
            testUser,
            "업무",
            "#FF0000",
            "설명"
        );

        // when & then
        assertThat(category.canBeDeleted()).isTrue();
    }

    @Test
    @DisplayName("색상 없이 카테고리 생성 후 유효성 검사")
    void createCategoryWithoutColor() {
        // given
        EventCategory category = new EventCategory(
            testUser,
            "업무",
            null,
            "업무 관련 일정"
        );

        // when & then
        assertThatNoException().isThrownBy(category::validateCategory);
    }
}