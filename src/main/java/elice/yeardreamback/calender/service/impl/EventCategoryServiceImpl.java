package elice.yeardreamback.calender.service.impl;

import elice.yeardreamback.calender.dto.EventCategoryRequest;
import elice.yeardreamback.calender.dto.EventCategoryResponse;
import elice.yeardreamback.calender.entity.EventCategory;
import elice.yeardreamback.calender.exception.CategoryNotFoundException;
import elice.yeardreamback.calender.mapper.EventCategoryMapper;
import elice.yeardreamback.calender.repository.EventCategoryRepository;
import elice.yeardreamback.calender.service.EventCategoryService;
import elice.yeardreamback.entity.User;
import elice.yeardreamback.exception.UserNotFoundException;
import elice.yeardreamback.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * EventCategoryService 구현체
 * 이벤트 카테고리 관련 비즈니스 로직을 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventCategoryServiceImpl implements EventCategoryService {

    private final EventCategoryRepository eventCategoryRepository;
    private final UserRepository userRepository;
    private final EventCategoryMapper eventCategoryMapper;

    private static final int MAX_CATEGORIES_PER_USER = 50; // 사용자당 최대 카테고리 수

    @Override
    @Transactional
    public EventCategoryResponse createCategory(String username, EventCategoryRequest request) {
        log.info("카테고리 생성 요청: 사용자={}, 이름={}", username, request.getName());

        // 사용자 조회
        User user = findUserByUsername(username);

        // 카테고리 개수 제한 확인
        long currentCount = eventCategoryRepository.countByUserUsername(username);
        if (currentCount >= MAX_CATEGORIES_PER_USER) {
            throw new IllegalArgumentException("사용자당 최대 " + MAX_CATEGORIES_PER_USER + "개의 카테고리만 생성할 수 있습니다");
        }

        // 카테고리 이름 중복 확인
        if (isDuplicateName(username, request.getName(), null)) {
            throw new IllegalArgumentException("이미 존재하는 카테고리 이름입니다: " + request.getName());
        }

        // 카테고리 생성
        EventCategory category = eventCategoryMapper.toEntity(request, user);
        EventCategory savedCategory = eventCategoryRepository.save(category);

        log.info("카테고리 생성 완료: ID={}, 사용자={}, 이름={}", savedCategory.getId(), username, request.getName());
        return eventCategoryMapper.toResponse(savedCategory);
    }

    @Override
    @Transactional
    public EventCategoryResponse updateCategory(String username, Long categoryId, EventCategoryRequest request) {
        log.info("카테고리 수정 요청: ID={}, 사용자={}, 이름={}", categoryId, username, request.getName());

        // 카테고리 조회 및 권한 확인
        EventCategory category = findCategoryByIdAndUsername(categoryId, username);

        // 카테고리 이름 중복 확인 (현재 카테고리 제외)
        if (isDuplicateName(username, request.getName(), categoryId)) {
            throw new IllegalArgumentException("이미 존재하는 카테고리 이름입니다: " + request.getName());
        }

        // 카테고리 업데이트
        eventCategoryMapper.updateEntity(category, request);
        EventCategory updatedCategory = eventCategoryRepository.save(category);

        log.info("카테고리 수정 완료: ID={}, 사용자={}, 이름={}", categoryId, username, request.getName());
        return eventCategoryMapper.toResponse(updatedCategory);
    }

    @Override
    @Transactional
    public void deleteCategory(String username, Long categoryId) {
        log.info("카테고리 삭제 요청: ID={}, 사용자={}", categoryId, username);

        // 카테고리 조회 및 권한 확인
        EventCategory category = findCategoryByIdAndUsername(categoryId, username);

        // 삭제 가능 여부 확인 (이벤트가 있는 카테고리는 삭제 불가)
        if (!category.canBeDeleted()) {
            throw new IllegalArgumentException("이벤트가 있는 카테고리는 삭제할 수 없습니다. 먼저 해당 카테고리의 모든 이벤트를 삭제하거나 다른 카테고리로 이동해주세요.");
        }

        // 카테고리 삭제
        eventCategoryRepository.delete(category);

        log.info("카테고리 삭제 완료: ID={}, 사용자={}", categoryId, username);
    }

    @Override
    public EventCategoryResponse getCategory(String username, Long categoryId) {
        log.debug("카테고리 조회 요청: ID={}, 사용자={}", categoryId, username);

        EventCategory category = findCategoryByIdAndUsername(categoryId, username);
        return eventCategoryMapper.toResponse(category);
    }

    @Override
    public List<EventCategoryResponse> getUserCategories(String username) {
        log.debug("사용자 전체 카테고리 조회: 사용자={}", username);

        List<EventCategory> categories = eventCategoryRepository.findByUserUsernameOrderByCreatedAtAsc(username);
        return eventCategoryMapper.toResponseList(categories);
    }

    @Override
    public List<EventCategoryResponse> getCategoriesWithEvents(String username) {
        log.debug("이벤트가 있는 카테고리 조회: 사용자={}", username);

        List<EventCategory> categories = eventCategoryRepository.findCategoriesWithEvents(username);
        return eventCategoryMapper.toResponseList(categories);
    }

    @Override
    public List<EventCategoryResponse> getCategoriesWithoutEvents(String username) {
        log.debug("이벤트가 없는 카테고리 조회: 사용자={}", username);

        List<EventCategory> categories = eventCategoryRepository.findCategoriesWithoutEvents(username);
        return eventCategoryMapper.toResponseList(categories);
    }

    @Override
    public boolean isDuplicateName(String username, String name, Long excludeCategoryId) {
        return eventCategoryRepository.existsByUserUsernameAndNameExcludingId(username, name, excludeCategoryId);
    }

    @Override
    public long countCategoriesByColor(String username, String color) {
        log.debug("색상별 카테고리 개수 조회: 사용자={}, 색상={}", username, color);

        return eventCategoryRepository.countByUserUsernameAndColor(username, color);
    }

    @Override
    public long getUserCategoryCount(String username) {
        log.debug("사용자 카테고리 개수 조회: 사용자={}", username);

        return eventCategoryRepository.countByUserUsername(username);
    }

    /**
     * 사용자명으로 사용자 조회
     */
    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: " + username));
    }

    /**
     * 카테고리 ID와 사용자명으로 카테고리 조회
     */
    private EventCategory findCategoryByIdAndUsername(Long categoryId, String username) {
        return eventCategoryRepository.findByIdAndUserUsername(categoryId, username)
            .orElseThrow(() -> new CategoryNotFoundException("카테고리를 찾을 수 없거나 접근 권한이 없습니다: " + categoryId));
    }
}