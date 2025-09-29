package elice.yeardreamback.calender.mapper;

import elice.yeardreamback.calender.dto.CalendarEventRequest;
import elice.yeardreamback.calender.dto.CalendarEventResponse;
import elice.yeardreamback.calender.entity.CalendarEvent;
import elice.yeardreamback.calender.entity.EventCategory;
import elice.yeardreamback.calender.enums.EventStatus;
import elice.yeardreamback.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * CalendarEventMapper 테스트
 */
@ExtendWith(MockitoExtension.class)
class CalendarEventMapperTest {

    @Mock
    private EventCategoryMapper categoryMapper;

    @Mock
    private EventReminderMapper reminderMapper;

    @InjectMocks
    private CalendarEventMapper calendarEventMapper;

    private User testUser;
    private EventCategory testCategory;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setName("테스트 사용자");

        testCategory = new EventCategory(testUser, "업무", "#FF0000", "업무 관련");
        testCategory.setId(1L);

        startTime = LocalDateTime.now().plusHours(1);
        endTime = startTime.plusHours(2);
    }

    @Test
    @DisplayName("CalendarEventRequest를 CalendarEvent 엔티티로 변환 테스트")
    void toEntity() {
        // given
        CalendarEventRequest request = new CalendarEventRequest(
            "테스트 이벤트",
            "테스트 설명",
            startTime,
            endTime,
            "테스트 장소",
            1L,
            Arrays.asList(30, 60)
        );

        // when
        CalendarEvent event = calendarEventMapper.toEntity(request, testUser, testCategory);

        // then
        assertThat(event).isNotNull();
        assertThat(event.getTitle()).isEqualTo("테스트 이벤트");
        assertThat(event.getDescription()).isEqualTo("테스트 설명");
        assertThat(event.getStartTime()).isEqualTo(startTime);
        assertThat(event.getEndTime()).isEqualTo(endTime);
        assertThat(event.getLocation()).isEqualTo("테스트 장소");
        assertThat(event.getUser()).isEqualTo(testUser);
        assertThat(event.getCategory()).isEqualTo(testCategory);
        assertThat(event.getStatus()).isEqualTo(EventStatus.SCHEDULED);
        assertThat(event.getReminders()).hasSize(2);
    }

    @Test
    @DisplayName("CalendarEvent 엔티티를 CalendarEventResponse로 변환 테스트 - 소유자")
    void toResponseAsOwner() {
        // given
        CalendarEvent event = new CalendarEvent(
            testUser,
            "테스트 이벤트",
            "테스트 설명",
            startTime,
            endTime,
            "테스트 장소",
            testCategory
        );
        event.setId(1L);

        when(categoryMapper.toResponse(any())).thenReturn(null);
        when(reminderMapper.toResponseList(any())).thenReturn(Arrays.asList());

        // when
        CalendarEventResponse response = calendarEventMapper.toResponse(event, "testuser");

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("테스트 이벤트");
        assertThat(response.getOwnerUsername()).isEqualTo("testuser");
        assertThat(response.isShared()).isFalse(); // 소유자이므로 공유가 아님
        assertThat(response.isCanEdit()).isTrue(); // 소유자이므로 편집 가능
    }

    @Test
    @DisplayName("CalendarEvent 엔티티를 CalendarEventResponse로 변환 테스트 - 다른 사용자")
    void toResponseAsOtherUser() {
        // given
        CalendarEvent event = new CalendarEvent(
            testUser,
            "테스트 이벤트",
            "테스트 설명",
            startTime,
            endTime,
            "테스트 장소",
            testCategory
        );
        event.setId(1L);

        when(categoryMapper.toResponse(any())).thenReturn(null);
        when(reminderMapper.toResponseList(any())).thenReturn(Arrays.asList());

        // when
        CalendarEventResponse response = calendarEventMapper.toResponse(event, "otheruser");

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("테스트 이벤트");
        assertThat(response.getOwnerUsername()).isEqualTo("testuser");
        assertThat(response.isShared()).isTrue(); // 다른 사용자이므로 공유됨
        assertThat(response.isCanEdit()).isFalse(); // 공유 권한이 없으므로 편집 불가
    }

    @Test
    @DisplayName("기존 CalendarEvent 엔티티 업데이트 테스트")
    void updateEntity() {
        // given
        CalendarEvent existingEvent = new CalendarEvent(
            testUser,
            "기존 이벤트",
            "기존 설명",
            startTime,
            endTime,
            "기존 장소",
            testCategory
        );

        CalendarEventRequest updateRequest = new CalendarEventRequest(
            "수정된 이벤트",
            "수정된 설명",
            startTime.plusHours(1),
            endTime.plusHours(1),
            "수정된 장소",
            1L,
            Arrays.asList(15, 30)
        );

        // when
        calendarEventMapper.updateEntity(existingEvent, updateRequest, testCategory);

        // then
        assertThat(existingEvent.getTitle()).isEqualTo("수정된 이벤트");
        assertThat(existingEvent.getDescription()).isEqualTo("수정된 설명");
        assertThat(existingEvent.getStartTime()).isEqualTo(startTime.plusHours(1));
        assertThat(existingEvent.getEndTime()).isEqualTo(endTime.plusHours(1));
        assertThat(existingEvent.getLocation()).isEqualTo("수정된 장소");
        assertThat(existingEvent.getReminders()).hasSize(2);
    }

    @Test
    @DisplayName("null 요청에 대한 안전한 처리 테스트")
    void handleNullRequest() {
        // when & then
        assertThat(calendarEventMapper.toEntity(null, testUser, testCategory)).isNull();
        assertThat(calendarEventMapper.toResponse(null, "testuser")).isNull();
        
        // updateEntity는 null 체크 후 아무것도 하지 않음
        CalendarEvent event = new CalendarEvent();
        calendarEventMapper.updateEntity(event, null, testCategory);
        calendarEventMapper.updateEntity(null, new CalendarEventRequest(), testCategory);
    }

    @Test
    @DisplayName("이벤트 리스트를 응답 리스트로 변환 테스트")
    void toResponseList() {
        // given
        CalendarEvent event1 = new CalendarEvent(testUser, "이벤트1", null, startTime, endTime, null, null);
        CalendarEvent event2 = new CalendarEvent(testUser, "이벤트2", null, startTime.plusDays(1), endTime.plusDays(1), null, null);
        List<CalendarEvent> events = Arrays.asList(event1, event2);

        when(categoryMapper.toResponse(any())).thenReturn(null);
        when(reminderMapper.toResponseList(any())).thenReturn(Arrays.asList());

        // when
        List<CalendarEventResponse> responses = calendarEventMapper.toResponseList(events, "testuser");

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getTitle()).isEqualTo("이벤트1");
        assertThat(responses.get(1).getTitle()).isEqualTo("이벤트2");
    }
}