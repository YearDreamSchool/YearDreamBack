package elice.yeardreamback.calender.controller;

import elice.yeardreamback.calender.dto.EventCategoryRequest;
import elice.yeardreamback.calender.dto.EventCategoryResponse;
import elice.yeardreamback.calender.service.EventCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 이벤트 카테고리 REST API 컨트롤러
 * 카테고리 CRUD 및 관리 기능을 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/calendar/categories")
@RequiredArgsConstructor
@Tag(name = "Event Categories", description = "이벤트 카테고리 관리 API")
public class EventCategoryController {

    private final EventCategoryService eventCategoryService;

    @Operation(
        summary = "새 카테고리 생성", 
        description = "새로운 이벤트 카테고리를 생성합니다. 사용자당 최대 50개까지 생성 가능하며, 카테고리 이름은 중복될 수 없습니다.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "생성할 카테고리 정보",
            required = true,
            content = @io.swagger.v3.oas.annotations.media.Content(
                mediaType = "application/json",
                schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = EventCategoryRequest.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    name = "카테고리 생성 예시",
                    value = """
                    {
                        "name": "업무",
                        "color": "#FF0000",
                        "description": "업무 관련 일정"
                    }
                    """
                )
            )
        )
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201", 
            description = "카테고리 생성 성공",
            content = @io.swagger.v3.oas.annotations.media.Content(
                mediaType = "application/json",
                schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = EventCategoryResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "잘못된 요청 데이터 (중복된 이름, 잘못된 색상 형식, 개수 제한 초과 등)",
            content = @io.swagger.v3.oas.annotations.media.Content(
                mediaType = "application/json",
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = """
                    {
                        "timestamp": "2024-12-01T10:00:00",
                        "status": 400,
                        "error": "Bad Request",
                        "message": "이미 존재하는 카테고리 이름입니다: 업무"
                    }
                    """
                )
            )
        ),
        @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @PostMapping
    public ResponseEntity<EventCategoryResponse> createCategory(
            @Valid @RequestBody EventCategoryRequest request,
            Authentication authentication) {
        
        log.info("카테고리 생성 요청: 사용자={}, 이름={}", authentication.getName(), request.getName());
        
        EventCategoryResponse response = eventCategoryService.createCategory(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "카테고리 수정", description = "기존 카테고리를 수정합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "카테고리 수정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터 또는 중복된 카테고리 이름"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없음")
    })
    @PutMapping("/{categoryId}")
    public ResponseEntity<EventCategoryResponse> updateCategory(
            @Parameter(description = "카테고리 ID") @PathVariable Long categoryId,
            @Valid @RequestBody EventCategoryRequest request,
            Authentication authentication) {
        
        log.info("카테고리 수정 요청: 사용자={}, 카테고리ID={}, 이름={}", 
                authentication.getName(), categoryId, request.getName());
        
        EventCategoryResponse response = eventCategoryService.updateCategory(
                authentication.getName(), categoryId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "카테고리 삭제", description = "카테고리를 삭제합니다. 이벤트가 있는 카테고리는 삭제할 수 없습니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "카테고리 삭제 성공"),
        @ApiResponse(responseCode = "400", description = "이벤트가 있는 카테고리는 삭제 불가"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없음")
    })
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "카테고리 ID") @PathVariable Long categoryId,
            Authentication authentication) {
        
        log.info("카테고리 삭제 요청: 사용자={}, 카테고리ID={}", authentication.getName(), categoryId);
        
        eventCategoryService.deleteCategory(authentication.getName(), categoryId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "카테고리 조회", description = "특정 카테고리의 상세 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "카테고리 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없음")
    })
    @GetMapping("/{categoryId}")
    public ResponseEntity<EventCategoryResponse> getCategory(
            @Parameter(description = "카테고리 ID") @PathVariable Long categoryId,
            Authentication authentication) {
        
        log.debug("카테고리 조회 요청: 사용자={}, 카테고리ID={}", authentication.getName(), categoryId);
        
        EventCategoryResponse response = eventCategoryService.getCategory(authentication.getName(), categoryId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "사용자 카테고리 목록 조회", description = "사용자의 모든 카테고리를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "카테고리 목록 조회 성공")
    @GetMapping
    public ResponseEntity<List<EventCategoryResponse>> getUserCategories(Authentication authentication) {
        log.debug("사용자 카테고리 목록 조회: 사용자={}", authentication.getName());
        
        List<EventCategoryResponse> responses = eventCategoryService.getUserCategories(authentication.getName());
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "이벤트가 있는 카테고리 조회", description = "이벤트가 있는 카테고리만 조회합니다 (삭제 불가능한 카테고리).")
    @ApiResponse(responseCode = "200", description = "이벤트가 있는 카테고리 목록 조회 성공")
    @GetMapping("/with-events")
    public ResponseEntity<List<EventCategoryResponse>> getCategoriesWithEvents(Authentication authentication) {
        log.debug("이벤트가 있는 카테고리 조회: 사용자={}", authentication.getName());
        
        List<EventCategoryResponse> responses = eventCategoryService.getCategoriesWithEvents(authentication.getName());
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "이벤트가 없는 카테고리 조회", description = "이벤트가 없는 카테고리만 조회합니다 (삭제 가능한 카테고리).")
    @ApiResponse(responseCode = "200", description = "이벤트가 없는 카테고리 목록 조회 성공")
    @GetMapping("/without-events")
    public ResponseEntity<List<EventCategoryResponse>> getCategoriesWithoutEvents(Authentication authentication) {
        log.debug("이벤트가 없는 카테고리 조회: 사용자={}", authentication.getName());
        
        List<EventCategoryResponse> responses = eventCategoryService.getCategoriesWithoutEvents(authentication.getName());
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "카테고리 이름 중복 확인", description = "카테고리 이름의 중복 여부를 확인합니다.")
    @ApiResponse(responseCode = "200", description = "중복 확인 완료")
    @GetMapping("/check-duplicate")
    public ResponseEntity<Boolean> checkDuplicateName(
            @Parameter(description = "확인할 카테고리 이름") @RequestParam String name,
            @Parameter(description = "제외할 카테고리 ID (수정 시 사용)") @RequestParam(required = false) Long excludeCategoryId,
            Authentication authentication) {
        
        log.debug("카테고리 이름 중복 확인: 사용자={}, 이름={}", authentication.getName(), name);
        
        boolean isDuplicate = eventCategoryService.isDuplicateName(
                authentication.getName(), name, excludeCategoryId);
        return ResponseEntity.ok(isDuplicate);
    }

    @Operation(summary = "색상별 카테고리 개수 조회", description = "특정 색상을 사용하는 카테고리 개수를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "색상별 카테고리 개수 조회 성공")
    @GetMapping("/count-by-color")
    public ResponseEntity<Long> countCategoriesByColor(
            @Parameter(description = "색상 코드 (예: #FF0000)") @RequestParam String color,
            Authentication authentication) {
        
        log.debug("색상별 카테고리 개수 조회: 사용자={}, 색상={}", authentication.getName(), color);
        
        long count = eventCategoryService.countCategoriesByColor(authentication.getName(), color);
        return ResponseEntity.ok(count);
    }

    @Operation(summary = "사용자 카테고리 총 개수 조회", description = "사용자의 총 카테고리 개수를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "카테고리 총 개수 조회 성공")
    @GetMapping("/count")
    public ResponseEntity<Long> getUserCategoryCount(Authentication authentication) {
        log.debug("사용자 카테고리 총 개수 조회: 사용자={}", authentication.getName());
        
        long count = eventCategoryService.getUserCategoryCount(authentication.getName());
        return ResponseEntity.ok(count);
    }
}