package elice.yeardreamback.calender.mapper;

import elice.yeardreamback.calender.dto.EventReminderResponse;
import elice.yeardreamback.calender.entity.EventReminder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * EventReminder 엔티티와 DTO 간 매핑을 담당하는 유틸리티 클래스
 */
@Component
public class EventReminderMapper {

    /**
     * EventReminder 엔티티를 EventReminderResponse로 변환
     * 
     * @param reminder 알림 엔티티
     * @return EventReminderResponse DTO
     */
    public EventReminderResponse toResponse(EventReminder reminder) {
        if (reminder == null) {
            return null;
        }

        return new EventReminderResponse(
            reminder.getId(),
            reminder.getMinutesBefore(),
            reminder.getIsActive(),
            reminder.getReminderTime(),
            reminder.isUpcoming(),
            reminder.getCreatedAt()
        );
    }

    /**
     * EventReminder 엔티티 리스트를 EventReminderResponse 리스트로 변환
     */
    public List<EventReminderResponse> toResponseList(List<EventReminder> reminders) {
        if (reminders == null) {
            return null;
        }

        return reminders.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    /**
     * 활성화된 알림만 응답으로 변환
     */
    public List<EventReminderResponse> toActiveResponseList(List<EventReminder> reminders) {
        if (reminders == null) {
            return null;
        }

        return reminders.stream()
            .filter(reminder -> reminder.getIsActive())
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
}