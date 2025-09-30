package elice.yeardreamback.calender.service.impl;

import elice.yeardreamback.calender.dto.EventReminderResponse;
import elice.yeardreamback.calender.entity.CalendarEvent;
import elice.yeardreamback.calender.entity.EventReminder;
import elice.yeardreamback.calender.exception.EventNotFoundException;
import elice.yeardreamback.calender.mapper.EventReminderMapper;
import elice.yeardreamback.calender.repository.CalendarEventRepository;
import elice.yeardreamback.calender.repository.EventReminderRepository;
import elice.yeardreamback.calender.service.EventReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * EventReminderService 구현체
 * 이벤트 알림 관련 비즈니스 로직을 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventReminderServiceImpl implements EventReminderService {

    private final EventReminderRepository eventReminderRepository;
    private final CalendarEventRepository calendarEventRepository;
    private final EventReminderMapper eventReminderMapper;

    @Override
    public List<EventReminderResponse> getEventReminders(String username, Long eventId) {
        log.debug("이벤트 알림 조회: 사용자={}, 이벤트ID={}", username, eventId);

        // 이벤트 존재 및 권한 확인
        validateEventAccess(username, eventId);

        List<EventReminder> reminders = eventReminderRepository.findByEventIdOrderByMinutesBeforeAsc(eventId);
        return eventReminderMapper.toResponseList(reminders);
    }

    @Override
    public List<EventReminderResponse> getActiveEventReminders(String username, Long eventId) {
        log.debug("이벤트 활성 알림 조회: 사용자={}, 이벤트ID={}", username, eventId);

        // 이벤트 존재 및 권한 확인
        validateEventAccess(username, eventId);

        List<EventReminder> reminders = eventReminderRepository.findByEventIdAndIsActiveTrueOrderByMinutesBeforeAsc(eventId);
        return eventReminderMapper.toResponseList(reminders);
    }

    @Override
    public List<EventReminderResponse> getUserActiveReminders(String username) {
        log.debug("사용자 활성 알림 조회: 사용자={}", username);

        List<EventReminder> reminders = eventReminderRepository.findActiveRemindersByUsername(username);
        return eventReminderMapper.toResponseList(reminders);
    }

    @Override
    public List<EventReminderResponse> getDailyReminders(String username, LocalDate date) {
        log.debug("일별 알림 조회: 사용자={}, 날짜={}", username, date);

        LocalDateTime dateTime = date.atStartOfDay();
        List<EventReminder> reminders = eventReminderRepository.findRemindersByUsernameAndDate(username, dateTime);
        return eventReminderMapper.toResponseList(reminders);
    }

    @Override
    @Transactional
    public EventReminderResponse activateReminder(String username, Long reminderId) {
        log.info("알림 활성화: 사용자={}, 알림ID={}", username, reminderId);

        EventReminder reminder = findReminderWithAccess(username, reminderId);
        reminder.activate();
        
        EventReminder savedReminder = eventReminderRepository.save(reminder);
        log.info("알림 활성화 완료: 알림ID={}", reminderId);
        
        return eventReminderMapper.toResponse(savedReminder);
    }

    @Override
    @Transactional
    public EventReminderResponse deactivateReminder(String username, Long reminderId) {
        log.info("알림 비활성화: 사용자={}, 알림ID={}", username, reminderId);

        EventReminder reminder = findReminderWithAccess(username, reminderId);
        reminder.deactivate();
        
        EventReminder savedReminder = eventReminderRepository.save(reminder);
        log.info("알림 비활성화 완료: 알림ID={}", reminderId);
        
        return eventReminderMapper.toResponse(savedReminder);
    }

    @Override
    public List<EventReminderResponse> getRemindersToSend(LocalDateTime startTime, LocalDateTime endTime) {
        log.debug("발송 대상 알림 조회: 시작={}, 종료={}", startTime, endTime);

        List<EventReminder> reminders = eventReminderRepository.findRemindersToSend(startTime, endTime);
        return eventReminderMapper.toResponseList(reminders);
    }

    @Override
    public List<EventReminderResponse> getRemindersByEventTimeAndMinutes(LocalDateTime eventStartTime, Integer minutesBefore) {
        log.debug("이벤트 시간별 알림 조회: 이벤트시간={}, 알림시간={}분전", eventStartTime, minutesBefore);

        List<EventReminder> reminders = eventReminderRepository.findRemindersByEventTimeAndMinutes(eventStartTime, minutesBefore);
        return eventReminderMapper.toResponseList(reminders);
    }

    @Override
    public List<EventReminderResponse> getRemindersForPastEvents(LocalDateTime currentTime) {
        log.debug("과거 이벤트 알림 조회: 기준시간={}", currentTime);

        List<EventReminder> reminders = eventReminderRepository.findRemindersForPastEvents(currentTime);
        return eventReminderMapper.toResponseList(reminders);
    }

    @Override
    public long getEventReminderCount(String username, Long eventId) {
        log.debug("이벤트 알림 개수 조회: 사용자={}, 이벤트ID={}", username, eventId);

        // 이벤트 존재 및 권한 확인
        validateEventAccess(username, eventId);

        return eventReminderRepository.countByEventId(eventId);
    }

    @Override
    public long getUserReminderCount(String username) {
        log.debug("사용자 총 알림 개수 조회: 사용자={}", username);

        return eventReminderRepository.countByUsername(username);
    }

    @Override
    public long getUpcomingReminderCount(String username) {
        log.debug("사용자 미래 알림 개수 조회: 사용자={}", username);

        List<EventReminder> activeReminders = eventReminderRepository.findActiveRemindersByUsername(username);
        return activeReminders.stream()
            .filter(EventReminder::isUpcoming)
            .count();
    }

    @Override
    @Transactional
    public long cleanupPastReminders(LocalDateTime beforeDate) {
        log.info("과거 알림 정리 시작: 기준날짜={}", beforeDate);

        List<EventReminder> pastReminders = eventReminderRepository.findRemindersForPastEvents(beforeDate);
        long count = pastReminders.size();
        
        if (count > 0) {
            eventReminderRepository.deleteAll(pastReminders);
            log.info("과거 알림 정리 완료: 삭제된 알림 수={}", count);
        } else {
            log.info("정리할 과거 알림이 없습니다");
        }
        
        return count;
    }

    /**
     * 이벤트 접근 권한 확인
     */
    private void validateEventAccess(String username, Long eventId) {
        CalendarEvent event = calendarEventRepository.findByIdAndUserUsername(eventId, username)
            .orElse(null);
        
        if (event == null) {
            // 소유한 이벤트가 아닌 경우 공유된 이벤트인지 확인
            event = calendarEventRepository.findSharedEventById(eventId, username)
                .orElseThrow(() -> new EventNotFoundException("이벤트를 찾을 수 없거나 접근 권한이 없습니다: " + eventId));
        }
    }

    /**
     * 알림 조회 및 접근 권한 확인
     */
    private EventReminder findReminderWithAccess(String username, Long reminderId) {
        EventReminder reminder = eventReminderRepository.findById(reminderId)
            .orElseThrow(() -> new EventNotFoundException("알림을 찾을 수 없습니다: " + reminderId));

        // 알림의 이벤트에 대한 접근 권한 확인
        validateEventAccess(username, reminder.getEvent().getId());
        
        return reminder;
    }
}