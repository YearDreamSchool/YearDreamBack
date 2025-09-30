package elice.yeardreamback.calender.service.impl;

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
import elice.yeardreamback.calender.service.AccessControlService;
import elice.yeardreamback.calender.service.CalendarEventService;
import elice.yeardreamback.user.entity.User;
import elice.yeardreamback.user.exception.UserNotFoundException;
import elice.yeardreamback.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;

/**
 * CalendarEventService 구현체
 * 캘린더 이벤트 관련 비즈니스 로직을 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CalendarEventServiceImpl implements CalendarEventService {

    private final CalendarEventRepository calendarEventRepository;
    private final EventCategoryRepository eventCategoryRepository;
    private final UserRepository userRepository;
    private final CalendarEventMapper calendarEventMapper;
    private final AccessControlService accessControlService;

    @Override
    @Transactional
    public CalendarEventResponse createEvent(String username, CalendarEventRequest request) {
        log.info("이벤트 생성 요청: 사용자={}, 제목={}", username, request.getTitle());

        // 사용자 조회
        User user = findUserByUsername(username);

        // 시간 범위 유효성 검사
        validateEventTimeRange(request.getStartTime(), request.getEndTime());

        // 카테고리 조회 (선택사항)
        EventCategory category = null;
        if (request.getCategoryId() != null) {
            category = findCategoryByIdAndUsername(request.getCategoryId(), username);
        }

        // 겹치는 이벤트 확인
        List<CalendarEvent> overlappingEvents = calendarEventRepository.findOverlappingEvents(
            username, request.getStartTime(), request.getEndTime(), null);
        
        if (!overlappingEvents.isEmpty()) {
            log.warn("겹치는 이벤트 발견: 사용자={}, 겹치는 이벤트 수={}", username, overlappingEvents.size());
            // 경고 로그만 남기고 계속 진행 (사용자가 의도적으로 겹치는 일정을 만들 수 있음)
        }

        // 이벤트 생성
        CalendarEvent event = calendarEventMapper.toEntity(request, user, category);
        CalendarEvent savedEvent = calendarEventRepository.save(event);

        log.info("이벤트 생성 완료: ID={}, 사용자={}", savedEvent.getId(), username);
        return calendarEventMapper.toResponse(savedEvent, username);
    }

    @Override
    @Transactional
    public CalendarEventResponse updateEvent(String username, Long eventId, CalendarEventRequest request) {
        log.info("이벤트 수정 요청: ID={}, 사용자={}", eventId, username);

        // 이벤트 편집 권한 확인
        CalendarEvent event = accessControlService.verifyEventEditAccess(username, eventId);

        // 시간 범위 유효성 검사
        validateEventTimeRange(request.getStartTime(), request.getEndTime());

        // 카테고리 조회 (선택사항)
        EventCategory category = null;
        if (request.getCategoryId() != null) {
            category = findCategoryByIdAndUsername(request.getCategoryId(), username);
        }

        // 겹치는 이벤트 확인 (현재 이벤트 제외)
        List<CalendarEvent> overlappingEvents = calendarEventRepository.findOverlappingEvents(
            username, request.getStartTime(), request.getEndTime(), eventId);
        
        if (!overlappingEvents.isEmpty()) {
            log.warn("겹치는 이벤트 발견: 사용자={}, 겹치는 이벤트 수={}", username, overlappingEvents.size());
        }

        // 이벤트 업데이트
        calendarEventMapper.updateEntity(event, request, category);
        CalendarEvent updatedEvent = calendarEventRepository.save(event);

        log.info("이벤트 수정 완료: ID={}, 사용자={}", eventId, username);
        return calendarEventMapper.toResponse(updatedEvent, username);
    }

    @Override
    @Transactional
    public void deleteEvent(String username, Long eventId) {
        log.info("이벤트 삭제 요청: ID={}, 사용자={}", eventId, username);

        // 이벤트 삭제 권한 확인 (소유자만 가능)
        CalendarEvent event = accessControlService.verifyEventDeleteAccess(username, eventId);

        // 이벤트 삭제
        calendarEventRepository.delete(event);

        log.info("이벤트 삭제 완료: ID={}, 사용자={}", eventId, username);
    }

    @Override
    public CalendarEventResponse getEvent(String username, Long eventId) {
        log.debug("이벤트 조회 요청: ID={}, 사용자={}", eventId, username);

        // 이벤트 읽기 권한 확인 (소유자 또는 공유받은 사용자)
        CalendarEvent event = accessControlService.verifyEventReadAccess(username, eventId);
        return calendarEventMapper.toResponse(event, username);
    }

    @Override
    public List<CalendarEventResponse> getUserEvents(String username) {
        log.debug("사용자 전체 이벤트 조회: 사용자={}", username);

        List<CalendarEvent> events = calendarEventRepository.findByUserUsernameOrderByStartTimeAsc(username);
        return calendarEventMapper.toResponseList(events, username);
    }

    @Override
    public List<CalendarEventResponse> getUserEvents(String username, LocalDate startDate, LocalDate endDate) {
        log.debug("날짜 범위 이벤트 조회: 사용자={}, 시작={}, 종료={}", username, startDate, endDate);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        List<CalendarEvent> events = calendarEventRepository
            .findByUserUsernameAndStartTimeBetweenOrderByStartTimeAsc(username, startDateTime, endDateTime);
        
        return calendarEventMapper.toResponseList(events, username);
    }

    @Override
    public List<CalendarEventResponse> getMonthlyEvents(String username, int year, int month) {
        log.debug("월별 이벤트 조회: 사용자={}, 년도={}, 월={}", username, year, month);

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        return getUserEvents(username, startDate, endDate);
    }

    @Override
    public List<CalendarEventResponse> getWeeklyEvents(String username, int year, int week) {
        log.debug("주별 이벤트 조회: 사용자={}, 년도={}, 주차={}", username, year, week);

        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        LocalDate startDate = LocalDate.of(year, 1, 1)
            .with(weekFields.weekOfYear(), week)
            .with(weekFields.dayOfWeek(), 1);
        LocalDate endDate = startDate.plusDays(6);

        return getUserEvents(username, startDate, endDate);
    }

    @Override
    public List<CalendarEventResponse> getDailyEvents(String username, LocalDate date) {
        log.debug("일별 이벤트 조회: 사용자={}, 날짜={}", username, date);

        LocalDateTime dateTime = date.atStartOfDay();
        List<CalendarEvent> events = calendarEventRepository.findByUserUsernameAndDate(username, dateTime);
        
        return calendarEventMapper.toResponseList(events, username);
    }

    @Override
    @Transactional
    public CalendarEventResponse updateEventStatus(String username, Long eventId, EventStatus status) {
        log.info("이벤트 상태 변경: ID={}, 사용자={}, 상태={}", eventId, username, status);

        CalendarEvent event = findEventByIdAndUsername(eventId, username);
        event.setStatus(status);
        
        CalendarEvent updatedEvent = calendarEventRepository.save(event);
        return calendarEventMapper.toResponse(updatedEvent, username);
    }

    @Override
    public List<CalendarEventResponse> getEventsByCategory(String username, Long categoryId) {
        log.debug("카테고리별 이벤트 조회: 사용자={}, 카테고리ID={}", username, categoryId);

        List<CalendarEvent> events = calendarEventRepository
            .findByUserUsernameAndCategoryIdOrderByStartTimeAsc(username, categoryId);
        
        return calendarEventMapper.toResponseList(events, username);
    }

    @Override
    public List<CalendarEventResponse> getOverlappingEvents(String username, LocalDateTime startTime, 
                                                          LocalDateTime endTime, Long excludeEventId) {
        log.debug("겹치는 이벤트 조회: 사용자={}, 시작={}, 종료={}", username, startTime, endTime);

        List<CalendarEvent> events = calendarEventRepository
            .findOverlappingEvents(username, startTime, endTime, excludeEventId);
        
        return calendarEventMapper.toResponseList(events, username);
    }

    @Override
    public List<CalendarEventResponse> getSharedEvents(String username) {
        log.debug("공유 이벤트 조회: 사용자={}", username);

        List<CalendarEvent> events = calendarEventRepository.findSharedEvents(username);
        return calendarEventMapper.toResponseList(events, username);
    }

    @Override
    public CalendarEventResponse getSharedEvent(String username, Long eventId) {
        log.debug("공유 이벤트 단건 조회: ID={}, 사용자={}", eventId, username);

        CalendarEvent event = calendarEventRepository.findSharedEventById(eventId, username)
            .orElseThrow(() -> new EventNotFoundException("공유된 이벤트를 찾을 수 없습니다: " + eventId));
        
        return calendarEventMapper.toResponse(event, username);
    }

    /**
     * 사용자명으로 사용자 조회
     */
    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: " + username));
    }

    /**
     * 이벤트 ID와 사용자명으로 이벤트 조회
     */
    private CalendarEvent findEventByIdAndUsername(Long eventId, String username) {
        return calendarEventRepository.findByIdAndUserUsername(eventId, username)
            .orElseThrow(() -> new EventNotFoundException("이벤트를 찾을 수 없거나 접근 권한이 없습니다: " + eventId));
    }

    /**
     * 카테고리 ID와 사용자명으로 카테고리 조회
     */
    private EventCategory findCategoryByIdAndUsername(Long categoryId, String username) {
        return eventCategoryRepository.findByIdAndUserUsername(categoryId, username)
            .orElseThrow(() -> new EventNotFoundException("카테고리를 찾을 수 없거나 접근 권한이 없습니다: " + categoryId));
    }

    /**
     * 이벤트 시간 범위 유효성 검사
     */
    private void validateEventTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new InvalidEventTimeException("이벤트 시작 시간과 종료 시간은 필수입니다");
        }
        
        if (!startTime.isBefore(endTime)) {
            throw new InvalidEventTimeException("이벤트 시작 시간은 종료 시간보다 이전이어야 합니다");
        }
        
        // 이벤트 기간이 너무 긴지 검사 (최대 7일)
        if (startTime.plusDays(7).isBefore(endTime)) {
            throw new InvalidEventTimeException("이벤트 기간은 최대 7일을 초과할 수 없습니다");
        }
    }
}