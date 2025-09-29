package elice.yeardreamback.calender.controller;

import elice.yeardreamback.calender.dto.EventShareRequest;
import elice.yeardreamback.calender.dto.EventShareResponse;
import elice.yeardreamback.calender.enums.SharePermission;
import elice.yeardreamback.calender.service.EventSharingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * EventSharingController 테스트
 */
@WebMvcTest(EventSharingController.class)
class EventSharingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventSharingService eventSharingService;

    @Autowired
    private ObjectMapper objectMapper;

    private EventShareRequest testRequest;
    private EventShareResponse testResponse;

    @BeforeEach
    void setUp() {
        testRequest = new EventShareRequest("shareduser", SharePermission.VIEW_ONLY);

        testResponse = new EventShareResponse();
        testResponse.setId(1L);
        testResponse.setEventId(1L);
        testResponse.setEventTitle("테스트 이벤트");
        testResponse.setSharedWithUsername("shareduser");
        testResponse.setSharedWithName("공유받는 사용자");
        testResponse.setPermission(SharePermission.VIEW_ONLY);
        testResponse.setEventOwnerUsername("owner");
        testResponse.setEventOwnerName("소유자");
        testResponse.setSharedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("이벤트 공유 성공 테스트")
    @WithMockUser(username = "owner")
    void shareEventSuccess() throws Exception {
        // given
        when(eventSharingService.shareEvent(eq("owner"), eq(1L), any(EventShareRequest.class)))
            .thenReturn(testResponse);

        // when & then
        mockMvc.perform(post("/api/calendar/events/1/share")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.eventId").value(1L))
                .andExpect(jsonPath("$.sharedWithUsername").value("shareduser"))
                .andExpect(jsonPath("$.permission").value("VIEW_ONLY"));

        verify(eventSharingService).shareEvent(eq("owner"), eq(1L), any(EventShareRequest.class));
    }

    @Test
    @DisplayName("유효하지 않은 데이터로 이벤트 공유 시 400 에러")
    @WithMockUser(username = "owner")
    void shareEventWithInvalidData() throws Exception {
        // given
        EventShareRequest invalidRequest = new EventShareRequest();
        invalidRequest.setSharedWithUsername(""); // 빈 사용자명
        invalidRequest.setPermission(null); // null 권한

        // when & then
        mockMvc.perform(post("/api/calendar/events/1/share")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(eventSharingService, never()).shareEvent(anyString(), anyLong(), any(EventShareRequest.class));
    }

    @Test
    @DisplayName("이벤트 공유 해제 성공 테스트")
    @WithMockUser(username = "owner")
    void unshareEventSuccess() throws Exception {
        // given
        doNothing().when(eventSharingService).unshareEvent("owner", 1L, "shareduser");

        // when & then
        mockMvc.perform(delete("/api/calendar/events/1/share/shareduser")
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(eventSharingService).unshareEvent("owner", 1L, "shareduser");
    }

    @Test
    @DisplayName("이벤트 공유 권한 변경 성공 테스트")
    @WithMockUser(username = "owner")
    void updateSharePermissionSuccess() throws Exception {
        // given
        testResponse.setPermission(SharePermission.EDIT);
        when(eventSharingService.updateSharePermission("owner", 1L, "shareduser", SharePermission.EDIT))
            .thenReturn(testResponse);

        // when & then
        mockMvc.perform(patch("/api/calendar/events/1/share/shareduser/permission")
                .with(csrf())
                .param("permission", "EDIT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.permission").value("EDIT"));

        verify(eventSharingService).updateSharePermission("owner", 1L, "shareduser", SharePermission.EDIT);
    }

    @Test
    @DisplayName("이벤트 공유 목록 조회 테스트")
    @WithMockUser(username = "owner")
    void getEventSharesSuccess() throws Exception {
        // given
        List<EventShareResponse> responses = Arrays.asList(testResponse);
        when(eventSharingService.getEventShares("owner", 1L)).thenReturn(responses);

        // when & then
        mockMvc.perform(get("/api/calendar/events/1/shares"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].sharedWithUsername").value("shareduser"));

        verify(eventSharingService).getEventShares("owner", 1L);
    }

    @Test
    @DisplayName("공유받은 이벤트 목록 조회 테스트")
    @WithMockUser(username = "shareduser")
    void getSharedWithUserEventsSuccess() throws Exception {
        // given
        List<EventShareResponse> responses = Arrays.asList(testResponse);
        when(eventSharingService.getSharedWithUserEvents("shareduser")).thenReturn(responses);

        // when & then
        mockMvc.perform(get("/api/calendar/events/shared-with-me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].eventOwnerUsername").value("owner"));

        verify(eventSharingService).getSharedWithUserEvents("shareduser");
    }

    @Test
    @DisplayName("내가 공유한 이벤트 목록 조회 테스트")
    @WithMockUser(username = "owner")
    void getOwnedEventSharesSuccess() throws Exception {
        // given
        List<EventShareResponse> responses = Arrays.asList(testResponse);
        when(eventSharingService.getOwnedEventShares("owner")).thenReturn(responses);

        // when & then
        mockMvc.perform(get("/api/calendar/events/shared-by-me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].sharedWithUsername").value("shareduser"));

        verify(eventSharingService).getOwnedEventShares("owner");
    }

    @Test
    @DisplayName("편집 가능한 공유 이벤트 조회 테스트")
    @WithMockUser(username = "shareduser")
    void getEditableSharedEventsSuccess() throws Exception {
        // given
        testResponse.setPermission(SharePermission.EDIT);
        List<EventShareResponse> responses = Arrays.asList(testResponse);
        when(eventSharingService.getEditableSharedEvents("shareduser")).thenReturn(responses);

        // when & then
        mockMvc.perform(get("/api/calendar/events/shared-editable"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].permission").value("EDIT"));

        verify(eventSharingService).getEditableSharedEvents("shareduser");
    }

    @Test
    @DisplayName("이벤트 공유 여부 확인 테스트")
    void isEventSharedWithSuccess() throws Exception {
        // given
        when(eventSharingService.isEventSharedWith(1L, "shareduser")).thenReturn(true);
        when(eventSharingService.isEventSharedWith(1L, "otheruser")).thenReturn(false);

        // when & then - 공유된 경우
        mockMvc.perform(get("/api/calendar/events/1/shared-with/shareduser"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        // when & then - 공유되지 않은 경우
        mockMvc.perform(get("/api/calendar/events/1/shared-with/otheruser"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(eventSharingService).isEventSharedWith(1L, "shareduser");
        verify(eventSharingService).isEventSharedWith(1L, "otheruser");
    }

    @Test
    @DisplayName("이벤트 공유 정보 조회 성공 테스트")
    void getEventShareSuccess() throws Exception {
        // given
        when(eventSharingService.getEventShare(1L, "shareduser")).thenReturn(testResponse);

        // when & then
        mockMvc.perform(get("/api/calendar/events/1/share-info/shareduser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.sharedWithUsername").value("shareduser"))
                .andExpect(jsonPath("$.permission").value("VIEW_ONLY"));

        verify(eventSharingService).getEventShare(1L, "shareduser");
    }

    @Test
    @DisplayName("존재하지 않는 공유 정보 조회 시 404 에러")
    void getEventShareNotFound() throws Exception {
        // given
        when(eventSharingService.getEventShare(1L, "nonexistent")).thenReturn(null);

        // when & then
        mockMvc.perform(get("/api/calendar/events/1/share-info/nonexistent"))
                .andExpect(status().isNotFound());

        verify(eventSharingService).getEventShare(1L, "nonexistent");
    }

    @Test
    @DisplayName("이벤트 공유 개수 조회 테스트")
    @WithMockUser(username = "owner")
    void getEventShareCountSuccess() throws Exception {
        // given
        when(eventSharingService.getEventShareCount("owner", 1L)).thenReturn(5L);

        // when & then
        mockMvc.perform(get("/api/calendar/events/1/share-count"))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));

        verify(eventSharingService).getEventShareCount("owner", 1L);
    }

    @Test
    @DisplayName("공유받은 이벤트 개수 조회 테스트")
    @WithMockUser(username = "shareduser")
    void getSharedWithUserEventCountSuccess() throws Exception {
        // given
        when(eventSharingService.getSharedWithUserEventCount("shareduser")).thenReturn(10L);

        // when & then
        mockMvc.perform(get("/api/calendar/events/shared-with-me/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("10"));

        verify(eventSharingService).getSharedWithUserEventCount("shareduser");
    }

    @Test
    @DisplayName("내가 공유한 총 개수 조회 테스트")
    @WithMockUser(username = "owner")
    void getOwnedEventShareCountSuccess() throws Exception {
        // given
        when(eventSharingService.getOwnedEventShareCount("owner")).thenReturn(15L);

        // when & then
        mockMvc.perform(get("/api/calendar/events/shared-by-me/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("15"));

        verify(eventSharingService).getOwnedEventShareCount("owner");
    }

    @Test
    @DisplayName("인증되지 않은 사용자 접근 시 401 에러")
    void accessWithoutAuthentication() throws Exception {
        // when & then
        mockMvc.perform(get("/api/calendar/events/shared-with-me"))
                .andExpect(status().isUnauthorized());

        verify(eventSharingService, never()).getSharedWithUserEvents(anyString());
    }

    @Test
    @DisplayName("빈 사용자명으로 공유 시 400 에러")
    @WithMockUser(username = "owner")
    void shareEventWithEmptyUsername() throws Exception {
        // given
        EventShareRequest invalidRequest = new EventShareRequest("", SharePermission.VIEW_ONLY);

        // when & then
        mockMvc.perform(post("/api/calendar/events/1/share")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(eventSharingService, never()).shareEvent(anyString(), anyLong(), any(EventShareRequest.class));
    }

    @Test
    @DisplayName("null 권한으로 공유 시 400 에러")
    @WithMockUser(username = "owner")
    void shareEventWithNullPermission() throws Exception {
        // given
        EventShareRequest invalidRequest = new EventShareRequest("shareduser", null);

        // when & then
        mockMvc.perform(post("/api/calendar/events/1/share")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(eventSharingService, never()).shareEvent(anyString(), anyLong(), any(EventShareRequest.class));
    }
}