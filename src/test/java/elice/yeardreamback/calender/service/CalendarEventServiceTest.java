package elice.yeardreamback.calender.service;

import elice.yeardreamback.calender.dto.CalendarEventRequest;
import elice.yeardreamback.calender.dto.CalendarEventResponse;
import elice.yeardreamback.calender.entity.CalendarEvent;
import elice.yeardreamback.calender.entity.EventCategory;
import elice.yeardreamback.calender.enums.EventStatus;
import elice.yeardreamback.calender.exception.EventNotFoundException;
import elice.yeardreamback.calender.exception.InvalidEventTimeException;
import elice.yeardreamback.calender.mapper.CalendarEventMapper;
import elice.yeardreamback.calender.repository.CalendarEventRepository;
import elice.yeardreamback.calender.repository.EventCategoryRepository;
import elice.yeardreamback.calender.service.impl.CalendarEventServiceImpl;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * CalendarEventService 테스트
 */
@ExtendWith(MockitoExtension.class)
class CalendarEventServiceTest {

    @Mock
    private CalendarEventRepository calendarEventRepository;

    @Mock
    private EventCategoryRepository eventCategoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CalendarEventMapper calendarEventMapper;

    @InjectMocks
    private CalendarEventServiceImpl calendarEventService;

    private User testUser;
    private EventCategory testCategory;
    private CalendarEvent testEvent;
    private CalendarEventRequest testRequest;
    private CalendarEventResponse testResponse;
    private LocalDateTime validStartTime;
    private LocalDateTime validEndTime;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setName("테스트 사용자");

        testCategory = new EventCategory(testUser, "업무", "#FF0000", "업무 관련");
        testCategory.setId(1L);

        validStartTime = LocalDateTime.now().plusHours(1);
        validEndTime = validStartTime.plusHours(2);

        testEvent = new CalendarEvent(
            testUser,
            "테스트 이벤트",
            "테스트 설명",
            validStartTime,
            validEndTime,
            "테스트 장소",
            testCategory
        );
        testEvent.setId(1L);

        testRequest = new CalendarEventRequest(
            "테스트 이벤트",
            "테스트 설명",
            validStartTime,
            validEndTime,
            "테스트 장소",
            1L,
            Arrays.asList(30, 60)
        );

        testResponse = new CalendarEventResponse();
        testResponse.setId(1L);
        testResponse.setTitle("테스트 이벤트");
    }

    @Test
    @DisplayName("이벤트 생성 성공 테스트")
    void createEventSuccess() {
        // given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(eventCategoryRepository.findByIdAndUserUsername(1L, "testuser")).thenReturn(Optional.of(testCategory));
        when(calendarEventRepository.findOverlappingEvents(anyString(), any(), any(), isNull())).thenReturn(Arrays.asList());
        when(calendarEventMapper.toEntity(testRequest, testUser, testCategory)).thenReturn(testEvent);
        when(calendarEventRepository.save(testEvent)).thenReturn(testEvent);
        when(calendarEventMapper.toResponse(testEvent, "testuser")).thenReturn(testResponse);

        // when
        CalendarEventResponse result = calendarEventService.createEvent("testuser", testRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(calendarEventRepository).save(testEvent);
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 이벤트 생성 시 예외 발생")
    void createEventWithNonExistentUser() {
        // given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> calendarEventService.createEvent("nonexistent", testRequest))
            .isInstanceOf(UserNotFoundException.class)
            .hasMessage("사용자를 찾을 수 없습니다: nonexistent");
    }

    @Test
    @DisplayName("잘못된 시간 범위로 이벤트 생성 시 예외 발생")
    void createEventWithInvalidTimeRange() {
        // given
        CalendarEventRequest invalidRequest = new CalendarEventRequest(
            "테스트 이벤트",
            "테스트 설명",
            validEndTime, // 시작과 종료 시간이 바뀜
            validStartTime,
            "테스트 장소",
            1L,
            Arrays.asList(30)
        );

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // when & then
        assertThatThrownBy(() -> calendarEventService.createEvent("testuser", invalidRequest))
            .isInstanceOf(InvalidEventTimeException.class)
            .hasMessage("이벤트 시작 시간은 종료 시간보다 이전이어야 합니다");
    }

    @Test
    @DisplayName("이벤트 수정 성공 테스트")
    void updateEventSuccess() {
        // given
        when(calendarEventRepository.findByIdAndUserUsername(1L, "testuser")).thenReturn(Optional.of(testEvent));
        when(eventCategoryRepository.findByIdAndUserUsername(1L, "testuser")).thenReturn(Optional.of(testCategory));
        when(calendarEventRepository.findOverlappingEvents(anyString(), any(), any(), eq(1L))).thenReturn(Arrays.asList());
        when(calendarEventRepository.save(testEvent)).thenReturn(testEvent);
        when(calendarEventMapper.toResponse(testEvent, "testuser")).thenReturn(testResponse);

        // when
        CalendarEventResponse result = calendarEventService.updateEvent("testuser", 1L, testRequest);

        // then
        assertThat(result).isNotNull();
        verify(calendarEventMapper).updateEntity(testEvent, testRequest, testCategory);
        verify(calendarEventRepository).save(testEvent);
    }

    @Test
    @DisplayName("존재하지 않는 이벤트 수정 시 예외 발생")
    void updateNonExistentEvent() {
        // given
        when(calendarEventRepository.findByIdAndUserUsername(999L, "testuser")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> calendarEventService.updateEvent("testuser", 999L, testRequest))
            .isInstanceOf(EventNotFoundException.class)
            .hasMessage("이벤트를 찾을 수 없거나 접근 권한이 없습니다: 999");
    }

    @Test
    @DisplayName("이벤트 삭제 성공 테스트")
    void deleteEventSuccess() {
        // given
        when(calendarEventRepository.findByIdAndUserUsername(1L, "testuser")).thenReturn(Optional.of(testEvent));

        // when
        calendarEventService.deleteEvent("testuser", 1L);

        // then
        verify(calendarEventRepository).delete(testEvent);
    }

    @Test
    @DisplayName("이벤트 조회 성공 테스트")
    void getEventSuccess() {
        // given
        when(calendarEventRepository.findByIdAndUserUsername(1L, "testuser")).thenReturn(Optional.of(testEvent));
        when(calendarEventMapper.toResponse(testEvent, "testuser")).thenReturn(testResponse);

        // when
        CalendarEventResponse result = calendarEventService.getEvent("testuser", 1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("사용자 전체 이벤트 조회 테스트")
    void getUserEventsSuccess() {
        // given
        List<CalendarEvent> events = Arrays.asList(testEvent);
        List<CalendarEventResponse> responses = Arrays.asList(testResponse);
        
        when(calendarEventRepository.findByUserUsernameOrderByStartTimeAsc("testuser")).thenReturn(events);
        when(calendarEventMapper.toResponseList(events, "testuser")).thenReturn(responses);

        // when
        List<CalendarEventResponse> result = calendarEventService.getUserEvents("testuser");

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("날짜 범위로 이벤트 조회 테스트")
    void getUserEventsByDateRangeSuccess() {
        // given
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(7);
        List<CalendarEvent> events = Arrays.asList(testEvent);
        List<CalendarEventResponse> responses = Arrays.asList(testResponse);
        
        when(calendarEventRepository.findByUserUsernameAndStartTimeBetweenOrderByStartTimeAsc(
            eq("testuser"), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(events);
        when(calendarEventMapper.toResponseList(events, "testuser")).thenReturn(responses);

        // when
        List<CalendarEventResponse> result = calendarEventService.getUserEvents("testuser", startDate, endDate);

        // then
        assertThat(result).hasSize(1);
        verify(calendarEventRepository).findByUserUsernameAndStartTimeBetweenOrderByStartTimeAsc(
            eq("testuser"), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("월별 이벤트 조회 테스트")
    void getMonthlyEventsSuccess() {
        // given
        List<CalendarEvent> events = Arrays.asList(testEvent);
        List<CalendarEventResponse> responses = Arrays.asList(testResponse);
        
        when(calendarEventRepository.findByUserUsernameAndStartTimeBetweenOrderByStartTimeAsc(
            eq("testuser"), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(events);
        when(calendarEventMapper.toResponseList(events, "testuser")).thenReturn(responses);

        // when
        List<CalendarEventResponse> result = calendarEventService.getMonthlyEvents("testuser", 2024, 12);

        // then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("이벤트 상태 변경 테스트")
    void updateEventStatusSuccess() {
        // given
        when(calendarEventRepository.findByIdAndUserUsername(1L, "testuser")).thenReturn(Optional.of(testEvent));
        when(calendarEventRepository.save(testEvent)).thenReturn(testEvent);
        when(calendarEventMapper.toResponse(testEvent, "testuser")).thenReturn(testResponse);

        // when
        CalendarEventResponse result = calendarEventService.updateEventStatus("testuser", 1L, EventStatus.COMPLETED);

        // then
        assertThat(result).isNotNull();
        verify(calendarEventRepository).save(testEvent);
        assertThat(testEvent.getStatus()).isEqualTo(EventStatus.COMPLETED);
    }

    @Test
    @DisplayName("겹치는 이벤트 조회 테스트")
    void getOverlappingEventsSuccess() {
        // given
        List<CalendarEvent> events = Arrays.asList(testEvent);
        List<CalendarEventResponse> responses = Arrays.asList(testResponse);
        
        when(calendarEventRepository.findOverlappingEvents("testuser", validStartTime, validEndTime, null))
            .thenReturn(events);
        when(calendarEventMapper.toResponseList(events, "testuser")).thenReturn(responses);

        // when
        List<CalendarEventResponse> result = calendarEventService.getOverlappingEvents(
            "testuser", validStartTime, validEndTime, null);

        // then
        assertThat(result).hasSize(1);
        verify(calendarEventRepository).findOverlappingEvents("testuser", validStartTime, validEndTime, null);
    }
}