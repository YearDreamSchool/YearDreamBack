package elice.yeardreamback.calender.controller;

import elice.yeardreamback.calender.dto.EventShareRequest;
import elice.yeardreamback.calender.dto.EventShareResponse;
import elice.yeardreamback.calender.enums.SharePermission;
import elice.yeardreamback.calender.service.EventSharingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
 * 이벤트 공유 REST API 컨트롤러
 * 이벤트 공유 및 권한 관리 기능을 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/calendar/events")
@RequiredArgsConstructor
@Tag(name = "Event Sharing", description = "이벤트 공유 관리 API")
public class EventSharingController {

    private final EventSharingService eventSharingService;

    @Operation(summary = "이벤트 공유", description = "이벤트를 다른 사용자와 공유합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "이벤트 공유 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터 또는 공유 제한 초과"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "404", description = "이벤트를 찾을 수 없음")
    })
    @PostMapping("/{eventId}/share")
    public ResponseEntity<EventShareResponse> shareEvent(
            @Parameter(description = "이벤트 ID") @PathVariable Long eventId,
            @Valid @RequestBody EventShareRequest request,
            Authentication authentication) {
        
        log.info("이벤트 공유 요청: 소유자={}, 이벤트ID={}, 공유대상={}, 권한={}", 
                authentication.getName(), eventId, request.getSharedWithUsername(), request.getPermission());
        
        EventShareResponse response = eventSharingService.shareEvent(
                authentication.getName(), eventId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "이벤트 공유 해제", description = "이벤트 공유를 해제합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "이벤트 공유 해제 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "404", description = "이벤트 또는 공유 정보를 찾을 수 없음")
    })
    @DeleteMapping("/{eventId}/share/{sharedWithUsername}")
    public ResponseEntity<Void> unshareEvent(
            @Parameter(description = "이벤트 ID") @PathVariable Long eventId,
            @Parameter(description = "공유 해제할 사용자명") @PathVariable String sharedWithUsername,
            Authentication authentication) {
        
        log.info("이벤트 공유 해제 요청: 소유자={}, 이벤트ID={}, 공유해제대상={}", 
                authentication.getName(), eventId, sharedWithUsername);
        
        eventSharingService.unshareEvent(authentication.getName(), eventId, sharedWithUsername);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "이벤트 공유 권한 변경", description = "이벤트 공유 권한을 변경합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "권한 변경 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "404", description = "이벤트 또는 공유 정보를 찾을 수 없음")
    })
    @PatchMapping("/{eventId}/share/{sharedWithUsername}/permission")
    public ResponseEntity<EventShareResponse> updateSharePermission(
            @Parameter(description = "이벤트 ID") @PathVariable Long eventId,
            @Parameter(description = "권한을 변경할 사용자명") @PathVariable String sharedWithUsername,
            @Parameter(description = "새로운 권한") @RequestParam SharePermission permission,
            Authentication authentication) {
        
        log.info("이벤트 공유 권한 변경: 소유자={}, 이벤트ID={}, 대상={}, 새권한={}", 
                authentication.getName(), eventId, sharedWithUsername, permission);
        
        EventShareResponse response = eventSharingService.updateSharePermission(
                authentication.getName(), eventId, sharedWithUsername, permission);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "이벤트 공유 목록 조회", description = "특정 이벤트의 모든 공유 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "공유 목록 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "404", description = "이벤트를 찾을 수 없음")
    })
    @GetMapping("/{eventId}/shares")
    public ResponseEntity<List<EventShareResponse>> getEventShares(
            @Parameter(description = "이벤트 ID") @PathVariable Long eventId,
            Authentication authentication) {
        
        log.debug("이벤트 공유 목록 조회: 소유자={}, 이벤트ID={}", authentication.getName(), eventId);
        
        List<EventShareResponse> responses = eventSharingService.getEventShares(
                authentication.getName(), eventId);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "공유받은 이벤트 목록 조회", description = "사용자가 공유받은 모든 이벤트를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "공유받은 이벤트 목록 조회 성공")
    @GetMapping("/shared-with-me")
    public ResponseEntity<List<EventShareResponse>> getSharedWithUserEvents(Authentication authentication) {
        log.debug("공유받은 이벤트 목록 조회: 사용자={}", authentication.getName());
        
        List<EventShareResponse> responses = eventSharingService.getSharedWithUserEvents(authentication.getName());
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "내가 공유한 이벤트 목록 조회", description = "사용자가 다른 사용자와 공유한 모든 이벤트를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "공유한 이벤트 목록 조회 성공")
    @GetMapping("/shared-by-me")
    public ResponseEntity<List<EventShareResponse>> getOwnedEventShares(Authentication authentication) {
        log.debug("내가 공유한 이벤트 목록 조회: 소유자={}", authentication.getName());
        
        List<EventShareResponse> responses = eventSharingService.getOwnedEventShares(authentication.getName());
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "편집 가능한 공유 이벤트 조회", description = "편집 권한으로 공유받은 이벤트를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "편집 가능한 공유 이벤트 조회 성공")
    @GetMapping("/shared-editable")
    public ResponseEntity<List<EventShareResponse>> getEditableSharedEvents(Authentication authentication) {
        log.debug("편집 가능한 공유 이벤트 조회: 사용자={}", authentication.getName());
        
        List<EventShareResponse> responses = eventSharingService.getEditableSharedEvents(authentication.getName());
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "이벤트 공유 여부 확인", description = "특정 이벤트가 특정 사용자와 공유되어 있는지 확인합니다.")
    @ApiResponse(responseCode = "200", description = "공유 여부 확인 완료")
    @GetMapping("/{eventId}/shared-with/{username}")
    public ResponseEntity<Boolean> isEventSharedWith(
            @Parameter(description = "이벤트 ID") @PathVariable Long eventId,
            @Parameter(description = "확인할 사용자명") @PathVariable String username) {
        
        log.debug("이벤트 공유 여부 확인: 이벤트ID={}, 사용자={}", eventId, username);
        
        boolean isShared = eventSharingService.isEventSharedWith(eventId, username);
        return ResponseEntity.ok(isShared);
    }

    @Operation(summary = "이벤트 공유 정보 조회", description = "특정 이벤트와 사용자 간의 공유 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "공유 정보 조회 성공"),
        @ApiResponse(responseCode = "404", description = "공유 정보를 찾을 수 없음")
    })
    @GetMapping("/{eventId}/share-info/{username}")
    public ResponseEntity<EventShareResponse> getEventShare(
            @Parameter(description = "이벤트 ID") @PathVariable Long eventId,
            @Parameter(description = "사용자명") @PathVariable String username) {
        
        log.debug("이벤트 공유 정보 조회: 이벤트ID={}, 사용자={}", eventId, username);
        
        EventShareResponse response = eventSharingService.getEventShare(eventId, username);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "이벤트 공유 개수 조회", description = "특정 이벤트의 공유 개수를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "공유 개수 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "404", description = "이벤트를 찾을 수 없음")
    })
    @GetMapping("/{eventId}/share-count")
    public ResponseEntity<Long> getEventShareCount(
            @Parameter(description = "이벤트 ID") @PathVariable Long eventId,
            Authentication authentication) {
        
        log.debug("이벤트 공유 개수 조회: 소유자={}, 이벤트ID={}", authentication.getName(), eventId);
        
        long count = eventSharingService.getEventShareCount(authentication.getName(), eventId);
        return ResponseEntity.ok(count);
    }

    @Operation(summary = "공유받은 이벤트 개수 조회", description = "사용자가 공유받은 이벤트 개수를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "공유받은 이벤트 개수 조회 성공")
    @GetMapping("/shared-with-me/count")
    public ResponseEntity<Long> getSharedWithUserEventCount(Authentication authentication) {
        log.debug("공유받은 이벤트 개수 조회: 사용자={}", authentication.getName());
        
        long count = eventSharingService.getSharedWithUserEventCount(authentication.getName());
        return ResponseEntity.ok(count);
    }

    @Operation(summary = "내가 공유한 총 개수 조회", description = "사용자가 소유한 이벤트의 총 공유 개수를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "총 공유 개수 조회 성공")
    @GetMapping("/shared-by-me/count")
    public ResponseEntity<Long> getOwnedEventShareCount(Authentication authentication) {
        log.debug("내가 공유한 총 개수 조회: 소유자={}", authentication.getName());
        
        long count = eventSharingService.getOwnedEventShareCount(authentication.getName());
        return ResponseEntity.ok(count);
    }
}