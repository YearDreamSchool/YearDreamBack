package elice.yeardreamback.calender.service;

import elice.yeardreamback.calender.dto.EventCategoryRequest;
import elice.yeardreamback.calender.dto.EventCategoryResponse;
import elice.yeardreamback.calender.entity.EventCategory;
import elice.yeardreamback.calender.exception.CategoryNotFoundException;
import elice.yeardreamback.calender.mapper.EventCategoryMapper;
import elice.yeardreamback.calender.repository.EventCategoryRepository;
import elice.yeardreamback.calender.service.impl.EventCategoryServiceImpl;
import elice.yeardreamback.entity.User;
import elice.yeardreamback.exception.UserNotFoundException;
import elice.yeardreamback.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * EventCategoryService 테스트
 */
@ExtendWith(MockitoExtension.class)
class EventCategoryServiceTest {

    @Mock
    private EventCategoryRepository eventCategoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventCategoryMapper eventCategoryMapper;

    @InjectMocks
    private EventCategoryServiceImpl eventCategoryService;

    private User testUser;
    private EventCategory testCategory;
    private EventCategoryRequest testRequest;
    private EventCategoryResponse testResponse;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setName("테스트 사용자");

        testCategory = new EventCategory(testUser, "업무", "#FF0000", "업무 관련");
        testCategory.setId(1L);

        testRequest = new EventCategoryRequest("업무", "#FF0000", "업무 관련");

        testResponse = new EventCategoryResponse();
        testResponse.setId(1L);
        testResponse.setName("업무");
        testResponse.setColor("#FF0000");
        testResponse.setDescription("업무 관련");
    }

    @Test
    @DisplayName("카테고리 생성 성공 테스트")
    void createCategorySuccess() {
        // given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(eventCategoryRepository.countByUserUsername("testuser")).thenReturn(5L);
        when(eventCategoryRepository.existsByUserUsernameAndNameExcludingId("testuser", "업무", null)).thenReturn(false);
        when(eventCategoryMapper.toEntity(testRequest, testUser)).thenReturn(testCategory);
        when(eventCategoryRepository.save(testCategory)).thenReturn(testCategory);
        when(eventCategoryMapper.toResponse(testCategory)).thenReturn(testResponse);

        // when
        EventCategoryResponse result = eventCategoryService.createCategory("testuser", testRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("업무");
        verify(eventCategoryRepository).save(testCategory);
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 카테고리 생성 시 예외 발생")
    void createCategoryWithNonExistentUser() {
        // given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> eventCategoryService.createCategory("nonexistent", testRequest))
            .isInstanceOf(UserNotFoundException.class)
            .hasMessage("사용자를 찾을 수 없습니다: nonexistent");
    }

    @Test
    @DisplayName("카테고리 개수 제한 초과 시 예외 발생")
    void createCategoryExceedsLimit() {
        // given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(eventCategoryRepository.countByUserUsername("testuser")).thenReturn(50L); // 최대 개수

        // when & then
        assertThatThrownBy(() -> eventCategoryService.createCategory("testuser", testRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("사용자당 최대 50개의 카테고리만 생성할 수 있습니다");
    }

    @Test
    @DisplayName("중복된 카테고리 이름으로 생성 시 예외 발생")
    void createCategoryWithDuplicateName() {
        // given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(eventCategoryRepository.countByUserUsername("testuser")).thenReturn(5L);
        when(eventCategoryRepository.existsByUserUsernameAndNameExcludingId("testuser", "업무", null)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> eventCategoryService.createCategory("testuser", testRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("이미 존재하는 카테고리 이름입니다: 업무");
    }

    @Test
    @DisplayName("카테고리 수정 성공 테스트")
    void updateCategorySuccess() {
        // given
        EventCategoryRequest updateRequest = new EventCategoryRequest("수정된 업무", "#00FF00", "수정된 설명");
        
        when(eventCategoryRepository.findByIdAndUserUsername(1L, "testuser")).thenReturn(Optional.of(testCategory));
        when(eventCategoryRepository.existsByUserUsernameAndNameExcludingId("testuser", "수정된 업무", 1L)).thenReturn(false);
        when(eventCategoryRepository.save(testCategory)).thenReturn(testCategory);
        when(eventCategoryMapper.toResponse(testCategory)).thenReturn(testResponse);

        // when
        EventCategoryResponse result = eventCategoryService.updateCategory("testuser", 1L, updateRequest);

        // then
        assertThat(result).isNotNull();
        verify(eventCategoryMapper).updateEntity(testCategory, updateRequest);
        verify(eventCategoryRepository).save(testCategory);
    }

    @Test
    @DisplayName("존재하지 않는 카테고리 수정 시 예외 발생")
    void updateNonExistentCategory() {
        // given
        when(eventCategoryRepository.findByIdAndUserUsername(999L, "testuser")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> eventCategoryService.updateCategory("testuser", 999L, testRequest))
            .isInstanceOf(CategoryNotFoundException.class)
            .hasMessage("카테고리를 찾을 수 없거나 접근 권한이 없습니다: 999");
    }

    @Test
    @DisplayName("카테고리 삭제 성공 테스트")
    void deleteCategorySuccess() {
        // given
        EventCategory deletableCategory = spy(testCategory);
        when(deletableCategory.canBeDeleted()).thenReturn(true);
        when(eventCategoryRepository.findByIdAndUserUsername(1L, "testuser")).thenReturn(Optional.of(deletableCategory));

        // when
        eventCategoryService.deleteCategory("testuser", 1L);

        // then
        verify(eventCategoryRepository).delete(deletableCategory);
    }

    @Test
    @DisplayName("이벤트가 있는 카테고리 삭제 시 예외 발생")
    void deleteCategoryWithEvents() {
        // given
        EventCategory categoryWithEvents = spy(testCategory);
        when(categoryWithEvents.canBeDeleted()).thenReturn(false);
        when(eventCategoryRepository.findByIdAndUserUsername(1L, "testuser")).thenReturn(Optional.of(categoryWithEvents));

        // when & then
        assertThatThrownBy(() -> eventCategoryService.deleteCategory("testuser", 1L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("이벤트가 있는 카테고리는 삭제할 수 없습니다. 먼저 해당 카테고리의 모든 이벤트를 삭제하거나 다른 카테고리로 이동해주세요.");
    }

    @Test
    @DisplayName("카테고리 조회 성공 테스트")
    void getCategorySuccess() {
        // given
        when(eventCategoryRepository.findByIdAndUserUsername(1L, "testuser")).thenReturn(Optional.of(testCategory));
        when(eventCategoryMapper.toResponse(testCategory)).thenReturn(testResponse);

        // when
        EventCategoryResponse result = eventCategoryService.getCategory("testuser", 1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("업무");
    }

    @Test
    @DisplayName("사용자 전체 카테고리 조회 테스트")
    void getUserCategoriesSuccess() {
        // given
        List<EventCategory> categories = Arrays.asList(testCategory);
        List<EventCategoryResponse> responses = Arrays.asList(testResponse);
        
        when(eventCategoryRepository.findByUserUsernameOrderByCreatedAtAsc("testuser")).thenReturn(categories);
        when(eventCategoryMapper.toResponseList(categories)).thenReturn(responses);

        // when
        List<EventCategoryResponse> result = eventCategoryService.getUserCategories("testuser");

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("업무");
    }

    @Test
    @DisplayName("이벤트가 있는 카테고리 조회 테스트")
    void getCategoriesWithEventsSuccess() {
        // given
        List<EventCategory> categories = Arrays.asList(testCategory);
        List<EventCategoryResponse> responses = Arrays.asList(testResponse);
        
        when(eventCategoryRepository.findCategoriesWithEvents("testuser")).thenReturn(categories);
        when(eventCategoryMapper.toResponseList(categories)).thenReturn(responses);

        // when
        List<EventCategoryResponse> result = eventCategoryService.getCategoriesWithEvents("testuser");

        // then
        assertThat(result).hasSize(1);
        verify(eventCategoryRepository).findCategoriesWithEvents("testuser");
    }

    @Test
    @DisplayName("이벤트가 없는 카테고리 조회 테스트")
    void getCategoriesWithoutEventsSuccess() {
        // given
        List<EventCategory> categories = Arrays.asList(testCategory);
        List<EventCategoryResponse> responses = Arrays.asList(testResponse);
        
        when(eventCategoryRepository.findCategoriesWithoutEvents("testuser")).thenReturn(categories);
        when(eventCategoryMapper.toResponseList(categories)).thenReturn(responses);

        // when
        List<EventCategoryResponse> result = eventCategoryService.getCategoriesWithoutEvents("testuser");

        // then
        assertThat(result).hasSize(1);
        verify(eventCategoryRepository).findCategoriesWithoutEvents("testuser");
    }

    @Test
    @DisplayName("카테고리 이름 중복 확인 테스트")
    void isDuplicateNameTest() {
        // given
        when(eventCategoryRepository.existsByUserUsernameAndNameExcludingId("testuser", "업무", null)).thenReturn(true);
        when(eventCategoryRepository.existsByUserUsernameAndNameExcludingId("testuser", "개인", null)).thenReturn(false);

        // when & then
        assertThat(eventCategoryService.isDuplicateName("testuser", "업무", null)).isTrue();
        assertThat(eventCategoryService.isDuplicateName("testuser", "개인", null)).isFalse();
    }

    @Test
    @DisplayName("색상별 카테고리 개수 조회 테스트")
    void countCategoriesByColorTest() {
        // given
        when(eventCategoryRepository.countByUserUsernameAndColor("testuser", "#FF0000")).thenReturn(3L);

        // when
        long count = eventCategoryService.countCategoriesByColor("testuser", "#FF0000");

        // then
        assertThat(count).isEqualTo(3L);
        verify(eventCategoryRepository).countByUserUsernameAndColor("testuser", "#FF0000");
    }

    @Test
    @DisplayName("사용자 카테고리 개수 조회 테스트")
    void getUserCategoryCountTest() {
        // given
        when(eventCategoryRepository.countByUserUsername("testuser")).thenReturn(10L);

        // when
        long count = eventCategoryService.getUserCategoryCount("testuser");

        // then
        assertThat(count).isEqualTo(10L);
        verify(eventCategoryRepository).countByUserUsername("testuser");
    }
}