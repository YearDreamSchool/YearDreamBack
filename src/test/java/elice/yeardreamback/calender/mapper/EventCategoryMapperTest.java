package elice.yeardreamback.calender.mapper;

import elice.yeardreamback.calender.dto.EventCategoryRequest;
import elice.yeardreamback.calender.dto.EventCategoryResponse;
import elice.yeardreamback.calender.entity.EventCategory;
import elice.yeardreamback.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * EventCategoryMapper 테스트
 */
class EventCategoryMapperTest {

    private EventCategoryMapper categoryMapper;
    private User testUser;

    @BeforeEach
    void setUp() {
        categoryMapper = new EventCategoryMapper();
        
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setName("테스트 사용자");
    }

    @Test
    @DisplayName("EventCategoryRequest를 EventCategory 엔티티로 변환 테스트")
    void toEntity() {
        // given
        EventCategoryRequest request = new EventCategoryRequest(
            "업무",
            "#FF0000",
            "업무 관련 일정"
        );

        // when
        EventCategory category = categoryMapper.toEntity(request, testUser);

        // then
        assertThat(category).isNotNull();
        assertThat(category.getName()).isEqualTo("업무");
        assertThat(category.getColor()).isEqualTo("#FF0000");
        assertThat(category.getDescription()).isEqualTo("업무 관련 일정");
        assertThat(category.getUser()).isEqualTo(testUser);
    }

    @Test
    @DisplayName("EventCategory 엔티티를 EventCategoryResponse로 변환 테스트")
    void toResponse() {
        // given
        EventCategory category = new EventCategory(testUser, "업무", "#FF0000", "업무 관련 일정");
        category.setId(1L);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());

        // when
        EventCategoryResponse response = categoryMapper.toResponse(category);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("업무");
        assertThat(response.getColor()).isEqualTo("#FF0000");
        assertThat(response.getDescription()).isEqualTo("업무 관련 일정");
        assertThat(response.getEventCount()).isEqualTo(0);
        assertThat(response.isCanBeDeleted()).isTrue();
        assertThat(response.getCreatedAt()).isNotNull();
        assertThat(response.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("기존 EventCategory 엔티티 업데이트 테스트")
    void updateEntity() {
        // given
        EventCategory existingCategory = new EventCategory(testUser, "기존 카테고리", "#000000", "기존 설명");
        EventCategoryRequest updateRequest = new EventCategoryRequest(
            "수정된 카테고리",
            "#FF0000",
            "수정된 설명"
        );

        // when
        categoryMapper.updateEntity(existingCategory, updateRequest);

        // then
        assertThat(existingCategory.getName()).isEqualTo("수정된 카테고리");
        assertThat(existingCategory.getColor()).isEqualTo("#FF0000");
        assertThat(existingCategory.getDescription()).isEqualTo("수정된 설명");
        assertThat(existingCategory.getUser()).isEqualTo(testUser); // 사용자는 변경되지 않음
    }

    @Test
    @DisplayName("카테고리 리스트를 응답 리스트로 변환 테스트")
    void toResponseList() {
        // given
        EventCategory category1 = new EventCategory(testUser, "업무", "#FF0000", "업무 관련");
        category1.setId(1L);
        EventCategory category2 = new EventCategory(testUser, "개인", "#00FF00", "개인 일정");
        category2.setId(2L);
        List<EventCategory> categories = Arrays.asList(category1, category2);

        // when
        List<EventCategoryResponse> responses = categoryMapper.toResponseList(categories);

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getName()).isEqualTo("업무");
        assertThat(responses.get(1).getName()).isEqualTo("개인");
    }

    @Test
    @DisplayName("간단한 카테고리 응답 생성 테스트")
    void toSimpleResponse() {
        // given
        EventCategory category = new EventCategory(testUser, "업무", "#FF0000", "업무 관련");
        category.setId(1L);

        // when
        EventCategoryResponse response = categoryMapper.toSimpleResponse(category);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("업무");
        assertThat(response.getColor()).isEqualTo("#FF0000");
        // 간단한 응답에서는 이벤트 개수, 삭제 가능 여부 등은 설정되지 않음
        assertThat(response.getEventCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("null 요청에 대한 안전한 처리 테스트")
    void handleNullRequest() {
        // when & then
        assertThat(categoryMapper.toEntity(null, testUser)).isNull();
        assertThat(categoryMapper.toResponse(null)).isNull();
        assertThat(categoryMapper.toResponseList(null)).isNull();
        assertThat(categoryMapper.toSimpleResponse(null)).isNull();
        
        // updateEntity는 null 체크 후 아무것도 하지 않음
        EventCategory category = new EventCategory();
        categoryMapper.updateEntity(category, null);
        categoryMapper.updateEntity(null, new EventCategoryRequest());
    }
}