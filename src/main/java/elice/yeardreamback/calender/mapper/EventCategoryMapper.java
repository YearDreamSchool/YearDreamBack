package elice.yeardreamback.calender.mapper;

import elice.yeardreamback.calender.dto.EventCategoryRequest;
import elice.yeardreamback.calender.dto.EventCategoryResponse;
import elice.yeardreamback.calender.entity.EventCategory;
import elice.yeardreamback.user.entity.User;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * EventCategory 엔티티와 DTO 간 매핑을 담당하는 유틸리티 클래스
 */
@Component
public class EventCategoryMapper {

    /**
     * EventCategoryRequest를 EventCategory 엔티티로 변환
     * 
     * @param request 요청 DTO
     * @param user 카테고리 소유자
     * @return EventCategory 엔티티
     */
    public EventCategory toEntity(EventCategoryRequest request, User user) {
        if (request == null) {
            return null;
        }

        return new EventCategory(
            user,
            request.getName(),
            request.getColor(),
            request.getDescription()
        );
    }

    /**
     * EventCategory 엔티티를 EventCategoryResponse로 변환
     * 
     * @param category 카테고리 엔티티
     * @return EventCategoryResponse DTO
     */
    public EventCategoryResponse toResponse(EventCategory category) {
        if (category == null) {
            return null;
        }

        return new EventCategoryResponse(
            category.getId(),
            category.getName(),
            category.getColor(),
            category.getDescription(),
            category.getEventCount(),
            category.canBeDeleted(),
            category.getCreatedAt(),
            category.getUpdatedAt()
        );
    }

    /**
     * EventCategory 엔티티 리스트를 EventCategoryResponse 리스트로 변환
     */
    public List<EventCategoryResponse> toResponseList(List<EventCategory> categories) {
        if (categories == null) {
            return null;
        }

        return categories.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    /**
     * 기존 EventCategory 엔티티를 EventCategoryRequest로 업데이트
     * 
     * @param category 업데이트할 카테고리 엔티티
     * @param request 업데이트 요청 DTO
     */
    public void updateEntity(EventCategory category, EventCategoryRequest request) {
        if (category == null || request == null) {
            return;
        }

        category.setName(request.getName());
        category.setColor(request.getColor());
        category.setDescription(request.getDescription());
    }

    /**
     * 간단한 카테고리 정보만 포함하는 응답 생성 (이벤트 목록 조회용)
     */
    public EventCategoryResponse toSimpleResponse(EventCategory category) {
        if (category == null) {
            return null;
        }

        EventCategoryResponse response = new EventCategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setColor(category.getColor());
        
        return response;
    }
}