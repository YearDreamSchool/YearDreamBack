package elice.yeardreamback.calender.service;

import elice.yeardreamback.calender.dto.EventReminderResponse;
import elice.yeardreamback.calender.entity.CalendarEvent;
import elice.yeardreamback.calender.entity.EventReminder;
import elice.yeardreamback.calender.exception.EventNotFoundException;
import elice.yeardreamback.calender.mapper.EventReminderMapper;
import elice.yeardreamback.calender.repository.CalendarEventRepository;
import elice.yeardreamback.calender.repository.EventReminderRepository;
import elice.yeardreamback.calender.service.impl.EventReminderServiceImpl;
import elice.yeardreamback.entity.User;
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
 * EventReminderService 테스트
 */
@ExtendWith(MockitoExtension.class)
class EventReminderServiceTest {

    @Mock
    private EventReminderRepository eventReminderRepository;

    @Mock
    private CalendarEventRepository calendarEventRepository;

    @Mock
    private EventReminderMapper eventReminderMapper;

    @InjectMocks
    private EventReminderServiceImpl eventReminderService;

    private User testUser;
    private CalendarEvent testEvent;
    private EventReminder testReminder;
    private EventReminderResponse testResponse;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setName("테스트 사용자");

        LocalDateTime startTime = LocalDateTime.now().plusHours(2);
        LocalDateTime endTime = startTime.plusHours(1);

        testEvent = new CalendarEvent(
            testUser,
            "테스트 이벤트",
            "테스트 설명",
            startTime,
            endTime,
            "테스트 장소",
            null
        );
        testEvent.setId(1L);

        testReminder = new EventReminder(testEvent, 30);
        testReminder.setId(1L);

        testResponse = new EventReminderResponse();
        testResponse.setId(1L);
        testResponse.setMinutesBefore(30);
        testResponse.setIsActive(true);
    }

    @Test
    @DisplayName("이벤트 알림 조회 성공 테스트")
    void getEventRemindersSuccess() {
        // given
        when(calendarEventRepository.findByIdAndUserUsername(1L, "testuser")).thenReturn(Optional.of(testEvent));
        when(eventReminderRepository.findByEventIdOrderByMinutesBeforeAsc(1L)).thenReturn(Arrays.asList(testReminder));
        when(eventReminderMapper.toResponseList(Arrays.asList(testReminder))).thenReturn(Arrays.asList(testResponse));

        // when
        List<EventReminderResponse> result = eventReminderService.getEventReminders("testuser", 1L);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getMinutesBefore()).isEqualTo(30);
    }

    @Test
    @DisplayName("접근 권한이 없는 이벤트의 알림 조회 시 예외 발생")
    void getEventRemindersWithoutAccess() {
        // given
        when(calendarEventRepository.findByIdAndUserUsername(1L, "testuser")).thenReturn(Optional.empty());
        when(calendarEventRepository.findSharedEventById(1L, "testuser")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> eventReminderService.getEventReminders("testuser", 1L))
            .isInstanceOf(EventNotFoundException.class)
            .hasMessage("이벤트를 찾을 수 없거나 접근 권한이 없습니다: 1");
    }

    @Test
    @DisplayName("이벤트 활성 알림 조회 성공 테스트")
    void getActiveEventRemindersSuccess() {
        // given
        when(calendarEventRepository.findByIdAndUserUsername(1L, "testuser")).thenReturn(Optional.of(testEvent));
        when(eventReminderRepository.findByEventIdAndIsActiveTrueOrderByMinutesBeforeAsc(1L))
            .thenReturn(Arrays.asList(testReminder));
        when(eventReminderMapper.toResponseList(Arrays.asList(testReminder))).thenReturn(Arrays.asList(testResponse));

        // when
        List<EventReminderResponse> result = eventReminderService.getActiveEventReminders("testuser", 1L);

        // then
        assertThat(result).hasSize(1);
        verify(eventReminderRepository).findByEventIdAndIsActiveTrueOrderByMinutesBeforeAsc(1L);
    }

    @Test
    @DisplayName("사용자 활성 알림 조회 성공 테스트")
    void getUserActiveRemindersSuccess() {
        // given
        when(eventReminderRepository.findActiveRemindersByUsername("testuser")).thenReturn(Arrays.asList(testReminder));
        when(eventReminderMapper.toResponseList(Arrays.asList(testReminder))).thenReturn(Arrays.asList(testResponse));

        // when
        List<EventReminderResponse> result = eventReminderService.getUserActiveReminders("testuser");

        // then
        assertThat(result).hasSize(1);
        verify(eventReminderRepository).findActiveRemindersByUsername("testuser");
    }

    @Test
    @DisplayName("일별 알림 조회 성공 테스트")
    void getDailyRemindersSuccess() {
        // given
        LocalDate testDate = LocalDate.now();
        when(eventReminderRepository.findRemindersByUsernameAndDate(eq("testuser"), any(LocalDateTime.class)))
            .thenReturn(Arrays.asList(testReminder));
        when(eventReminderMapper.toResponseList(Arrays.asList(testReminder))).thenReturn(Arrays.asList(testResponse));

        // when
        List<EventReminderResponse> result = eventReminderService.getDailyReminders("testuser", testDate);

        // then
        assertThat(result).hasSize(1);
        verify(eventReminderRepository).findRemindersByUsernameAndDate(eq("testuser"), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("알림 활성화 성공 테스트")
    void activateReminderSuccess() {
        // given
        when(eventReminderRepository.findById(1L)).thenReturn(Optional.of(testReminder));
        when(calendarEventRepository.findByIdAndUserUsername(1L, "testuser")).thenReturn(Optional.of(testEvent));
        when(eventReminderRepository.save(testReminder)).thenReturn(testReminder);
        when(eventReminderMapper.toResponse(testReminder)).thenReturn(testResponse);

        // when
        EventReminderResponse result = eventReminderService.activateReminder("testuser", 1L);

        // then
        assertThat(result).isNotNull();
        verify(testReminder).activate();
        verify(eventReminderRepository).save(testReminder);
    }

    @Test
    @DisplayName("알림 비활성화 성공 테스트")
    void deactivateReminderSuccess() {
        // given
        when(eventReminderRepository.findById(1L)).thenReturn(Optional.of(testReminder));
        when(calendarEventRepository.findByIdAndUserUsername(1L, "testuser")).thenReturn(Optional.of(testEvent));
        when(eventReminderRepository.save(testReminder)).thenReturn(testReminder);
        when(eventReminderMapper.toResponse(testReminder)).thenReturn(testResponse);

        // when
        EventReminderResponse result = eventReminderService.deactivateReminder("testuser", 1L);

        // then
        assertThat(result).isNotNull();
        verify(testReminder).deactivate();
        verify(eventReminderRepository).save(testReminder);
    }

    @Test
    @DisplayName("존재하지 않는 알림 활성화 시 예외 발생")
    void activateNonExistentReminder() {
        // given
        when(eventReminderRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> eventReminderService.activateReminder("testuser", 999L))
            .isInstanceOf(EventNotFoundException.class)
            .hasMessage("알림을 찾을 수 없습니다: 999");
    }

    @Test
    @DisplayName("발송 대상 알림 조회 테스트")
    void getRemindersToSendSuccess() {
        // given
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusHours(1);
        
        when(eventReminderRepository.findRemindersToSend(startTime, endTime)).thenReturn(Arrays.asList(testReminder));
        when(eventReminderMapper.toResponseList(Arrays.asList(testReminder))).thenReturn(Arrays.asList(testResponse));

        // when
        List<EventReminderResponse> result = eventReminderService.getRemindersToSend(startTime, endTime);

        // then
        assertThat(result).hasSize(1);
        verify(eventReminderRepository).findRemindersToSend(startTime, endTime);
    }

    @Test
    @DisplayName("이벤트 시간별 알림 조회 테스트")
    void getRemindersByEventTimeAndMinutesSuccess() {
        // given
        LocalDateTime eventStartTime = LocalDateTime.now().plusHours(2);
        Integer minutesBefore = 30;
        
        when(eventReminderRepository.findRemindersByEventTimeAndMinutes(eventStartTime, minutesBefore))
            .thenReturn(Arrays.asList(testReminder));
        when(eventReminderMapper.toResponseList(Arrays.asList(testReminder))).thenReturn(Arrays.asList(testResponse));

        // when
        List<EventReminderResponse> result = eventReminderService.getRemindersByEventTimeAndMinutes(eventStartTime, minutesBefore);

        // then
        assertThat(result).hasSize(1);
        verify(eventReminderRepository).findRemindersByEventTimeAndMinutes(eventStartTime, minutesBefore);
    }

    @Test
    @DisplayName("과거 이벤트 알림 조회 테스트")
    void getRemindersForPastEventsSuccess() {
        // given
        LocalDateTime currentTime = LocalDateTime.now();
        
        when(eventReminderRepository.findRemindersForPastEvents(currentTime)).thenReturn(Arrays.asList(testReminder));
        when(eventReminderMapper.toResponseList(Arrays.asList(testReminder))).thenReturn(Arrays.asList(testResponse));

        // when
        List<EventReminderResponse> result = eventReminderService.getRemindersForPastEvents(currentTime);

        // then
        assertThat(result).hasSize(1);
        verify(eventReminderRepository).findRemindersForPastEvents(currentTime);
    }

    @Test
    @DisplayName("이벤트 알림 개수 조회 테스트")
    void getEventReminderCountSuccess() {
        // given
        when(calendarEventRepository.findByIdAndUserUsername(1L, "testuser")).thenReturn(Optional.of(testEvent));
        when(eventReminderRepository.countByEventId(1L)).thenReturn(3L);

        // when
        long count = eventReminderService.getEventReminderCount("testuser", 1L);

        // then
        assertThat(count).isEqualTo(3L);
        verify(eventReminderRepository).countByEventId(1L);
    }

    @Test
    @DisplayName("사용자 총 알림 개수 조회 테스트")
    void getUserReminderCountSuccess() {
        // given
        when(eventReminderRepository.countByUsername("testuser")).thenReturn(10L);

        // when
        long count = eventReminderService.getUserReminderCount("testuser");

        // then
        assertThat(count).isEqualTo(10L);
        verify(eventReminderRepository).countByUsername("testuser");
    }

    @Test
    @DisplayName("미래 알림 개수 조회 테스트")
    void getUpcomingReminderCountSuccess() {
        // given
        EventReminder upcomingReminder = spy(testReminder);
        when(upcomingReminder.isUpcoming()).thenReturn(true);
        
        when(eventReminderRepository.findActiveRemindersByUsername("testuser"))
            .thenReturn(Arrays.asList(upcomingReminder));

        // when
        long count = eventReminderService.getUpcomingReminderCount("testuser");

        // then
        assertThat(count).isEqualTo(1L);
        verify(upcomingReminder).isUpcoming();
    }

    @Test
    @DisplayName("과거 알림 정리 테스트")
    void cleanupPastRemindersSuccess() {
        // given
        LocalDateTime beforeDate = LocalDateTime.now().minusDays(30);
        List<EventReminder> pastReminders = Arrays.asList(testReminder);
        
        when(eventReminderRepository.findRemindersForPastEvents(beforeDate)).thenReturn(pastReminders);

        // when
        long deletedCount = eventReminderService.cleanupPastReminders(beforeDate);

        // then
        assertThat(deletedCount).isEqualTo(1L);
        verify(eventReminderRepository).deleteAll(pastReminders);
    }

    @Test
    @DisplayName("정리할 과거 알림이 없는 경우 테스트")
    void cleanupPastRemindersWithNoData() {
        // given
        LocalDateTime beforeDate = LocalDateTime.now().minusDays(30);
        
        when(eventReminderRepository.findRemindersForPastEvents(beforeDate)).thenReturn(Arrays.asList());

        // when
        long deletedCount = eventReminderService.cleanupPastReminders(beforeDate);

        // then
        assertThat(deletedCount).isEqualTo(0L);
        verify(eventReminderRepository, never()).deleteAll(any());
    }
}