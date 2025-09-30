package elice.yeardreamback.calender.mapper;

import elice.yeardreamback.calender.dto.EventShareResponse;
import elice.yeardreamback.calender.entity.EventShare;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * EventShare 엔티티와 DTO 간 매핑을 담당하는 유틸리티 클래스
 */
@Component
public class EventShareMapper {

    /**
     * EventShare 엔티티를 EventShareResponse로 변환
     * 
     * @param share 공유 엔티티
     * @return EventShareResponse DTO
     */
    public EventShareResponse toResponse(EventShare share) {
        if (share == null) {
            return null;
        }

        return new EventShareResponse(
            share.getId(),
            share.getEvent().getId(),
            share.getEvent().getTitle(),
            share.getSharedWithUser().getUsername(),
            share.getSharedWithUser().getName(),
            share.getPermission(),
            share.getEvent().getUser().getUsername(),
            share.getEvent().getUser().getName(),
            share.getSharedAt()
        );
    }

    /**
     * EventShare 엔티티 리스트를 EventShareResponse 리스트로 변환
     */
    public List<EventShareResponse> toResponseList(List<EventShare> shares) {
        if (shares == null) {
            return null;
        }

        return shares.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    /**
     * 특정 사용자와 공유된 이벤트만 응답으로 변환
     */
    public List<EventShareResponse> toSharedWithUserResponseList(List<EventShare> shares, String username) {
        if (shares == null) {
            return null;
        }

        return shares.stream()
            .filter(share -> share.getSharedWithUser().getUsername().equals(username))
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    /**
     * 특정 사용자가 소유한 이벤트의 공유만 응답으로 변환
     */
    public List<EventShareResponse> toOwnedEventShareResponseList(List<EventShare> shares, String ownerUsername) {
        if (shares == null) {
            return null;
        }

        return shares.stream()
            .filter(share -> share.getEvent().getUser().getUsername().equals(ownerUsername))
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
}