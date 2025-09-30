package elice.yeardreamback.calender.mapper;

import elice.yeardreamback.calender.dto.CalendarEventRequest;
import elice.yeardreamback.calender.dto.CalendarEventResponse;
import elice.yeardreamback.calender.entity.CalendarEvent;
import elice.yeardreamback.calender.entity.EventCategory;
import elice.yeardreamback.calender.entity.EventReminder;
import elice.yeardreamback.user.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * CalendarEvent 엔티티와 DTO 간 매핑을 담당하는 유틸리티 클래스
 */
@Component
public class CalendarEventMapper {

    private final EventCategoryMapper categoryMapper;
    private final EventReminderMapper reminderMapper;

    public CalendarEventMapper(EventCategoryMapper categoryMapper, EventReminderMapper reminderMapper) {
        this.categoryMapper = categoryMapper;
        this.reminderMapper = reminderMapper;
    }

    /**
     * CalendarEventRequest를 CalendarEvent 엔티티로 변환
     * 
     * @param request 요청 DTO
     * @param user 이벤트 소유자
     * @param category 이벤트 카테고리 (null 가능)
     * @return CalendarEvent 엔티티
     */
    public CalendarEvent toEntity(CalendarEventRequest request, User user, EventCategory category) {
        if (request == null) {
            return null;
        }

        CalendarEvent event = new CalendarEvent(
            user,
            request.getTitle(),
            request.getDescription(),
            request.getStartTime(),
            request.getEndTime(),
            request.getLocation(),
            category
        );

        // 알림 설정이 있는 경우 추가
        if (request.getReminderMinutes() != null && !request.getReminderMinutes().isEmpty()) {
            for (Integer minutes : request.getReminderMinutes()) {
                EventReminder reminder = new EventReminder(event, minutes);
                event.addReminder(reminder);
            }
        }

        return event;
    }

    /**
     * CalendarEvent 엔티티를 CalendarEventResponse로 변환
     * 
     * @param event 이벤트 엔티티
     * @param currentUsername 현재 사용자명 (공유 여부 확인용)
     * @return CalendarEventResponse DTO
     */
    public CalendarEventResponse toResponse(CalendarEvent event, String currentUsername) {
        if (event == null) {
            return null;
        }

        // 공유 여부 및 편집 권한 확인
        boolean isShared = !event.getUser().getUsername().equals(currentUsername);
        boolean canEdit = !isShared; // 소유자는 항상 편집 가능

        // 공유된 이벤트인 경우 편집 권한 확인
        if (isShared) {
            canEdit = event.getShares().stream()
                .anyMatch(share -> share.getSharedWithUser().getUsername().equals(currentUsername) 
                    && share.canEdit());
        }

        return new CalendarEventResponse(
            event.getId(),
            event.getTitle(),
            event.getDescription(),
            event.getStartTime(),
            event.getEndTime(),
            event.getLocation(),
            event.getStatus(),
            categoryMapper.toResponse(event.getCategory()),
            reminderMapper.toResponseList(event.getReminders()),
            event.getUser().getUsername(),
            isShared,
            canEdit,
            event.getCreatedAt(),
            event.getUpdatedAt()
        );
    }

    /**
     * CalendarEvent 엔티티 리스트를 CalendarEventResponse 리스트로 변환
     */
    public List<CalendarEventResponse> toResponseList(List<CalendarEvent> events, String currentUsername) {
        if (events == null) {
            return null;
        }

        return events.stream()
            .map(event -> toResponse(event, currentUsername))
            .collect(Collectors.toList());
    }

    /**
     * 기존 CalendarEvent 엔티티를 CalendarEventRequest로 업데이트
     * 
     * @param event 업데이트할 이벤트 엔티티
     * @param request 업데이트 요청 DTO
     * @param category 새로운 카테고리 (null 가능)
     */
    public void updateEntity(CalendarEvent event, CalendarEventRequest request, EventCategory category) {
        if (event == null || request == null) {
            return;
        }

        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setStartTime(request.getStartTime());
        event.setEndTime(request.getEndTime());
        event.setLocation(request.getLocation());
        event.setCategory(category);

        // 기존 알림 제거
        event.getReminders().clear();

        // 새로운 알림 추가
        if (request.getReminderMinutes() != null && !request.getReminderMinutes().isEmpty()) {
            for (Integer minutes : request.getReminderMinutes()) {
                EventReminder reminder = new EventReminder(event, minutes);
                event.addReminder(reminder);
            }
        }
    }

    /**
     * 간단한 이벤트 정보만 포함하는 응답 생성 (목록 조회용)
     */
    public CalendarEventResponse toSimpleResponse(CalendarEvent event, String currentUsername) {
        if (event == null) {
            return null;
        }

        boolean isShared = !event.getUser().getUsername().equals(currentUsername);
        boolean canEdit = !isShared;

        if (isShared) {
            canEdit = event.getShares().stream()
                .anyMatch(share -> share.getSharedWithUser().getUsername().equals(currentUsername) 
                    && share.canEdit());
        }

        CalendarEventResponse response = new CalendarEventResponse();
        response.setId(event.getId());
        response.setTitle(event.getTitle());
        response.setStartTime(event.getStartTime());
        response.setEndTime(event.getEndTime());
        response.setStatus(event.getStatus());
        response.setOwnerUsername(event.getUser().getUsername());
        response.setShared(isShared);
        response.setCanEdit(canEdit);

        // 카테고리 정보 (간단히)
        if (event.getCategory() != null) {
            response.setCategory(categoryMapper.toSimpleResponse(event.getCategory()));
        }

        return response;
    }
}