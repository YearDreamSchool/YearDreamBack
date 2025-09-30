package elice.yeardreamback.calender.service;

import elice.yeardreamback.calender.dto.EventCategoryRequest;
import elice.yeardreamback.calender.dto.EventCategoryResponse;

import java.util.List;

/**
 * 이벤트 카테고리 서비스 인터페이스
 * 카테고리 CRUD 및 비즈니스 로직을 담당
 */
public interface EventCategoryService {

    /**
     * 새로운 카테고리 생성
     * 
     * @param username 사용자명
     * @param request 카테고리 생성 요청
     * @return 생성된 카테고리 응답
     */
    EventCategoryResponse createCategory(String username, EventCategoryRequest request);

    /**
     * 기존 카테고리 수정
     * 
     * @param username 사용자명
     * @param categoryId 카테고리 ID
     * @param request 카테고리 수정 요청
     * @return 수정된 카테고리 응답
     */
    EventCategoryResponse updateCategory(String username, Long categoryId, EventCategoryRequest request);

    /**
     * 카테고리 삭제
     * 
     * @param username 사용자명
     * @param categoryId 카테고리 ID
     */
    void deleteCategory(String username, Long categoryId);

    /**
     * 특정 카테고리 조회
     * 
     * @param username 사용자명
     * @param categoryId 카테고리 ID
     * @return 카테고리 응답
     */
    EventCategoryResponse getCategory(String username, Long categoryId);

    /**
     * 사용자의 모든 카테고리 조회
     * 
     * @param username 사용자명
     * @return 카테고리 목록
     */
    List<EventCategoryResponse> getUserCategories(String username);

    /**
     * 이벤트가 있는 카테고리 조회 (삭제 불가능한 카테고리)
     * 
     * @param username 사용자명
     * @return 카테고리 목록
     */
    List<EventCategoryResponse> getCategoriesWithEvents(String username);

    /**
     * 이벤트가 없는 카테고리 조회 (삭제 가능한 카테고리)
     * 
     * @param username 사용자명
     * @return 카테고리 목록
     */
    List<EventCategoryResponse> getCategoriesWithoutEvents(String username);

    /**
     * 카테고리 이름 중복 확인
     * 
     * @param username 사용자명
     * @param name 카테고리 이름
     * @param excludeCategoryId 제외할 카테고리 ID (수정 시 사용)
     * @return 중복 여부
     */
    boolean isDuplicateName(String username, String name, Long excludeCategoryId);

    /**
     * 특정 색상을 사용하는 카테고리 개수 조회
     * 
     * @param username 사용자명
     * @param color 색상 코드
     * @return 카테고리 개수
     */
    long countCategoriesByColor(String username, String color);

    /**
     * 사용자의 총 카테고리 개수 조회
     * 
     * @param username 사용자명
     * @return 카테고리 개수
     */
    long getUserCategoryCount(String username);
}