package elice.yeardreamback.calender.controller;

import elice.yeardreamback.calender.dto.CalendarEventRequest;
import elice.yeardreamback.calender.dto.CalendarEventResponse;
import elice.yeardreamback.calender.enums.EventStatus;
import elice.yeardreamback.calender.service.CalendarEventService;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * CalendarEventController 테스트
 */
@WebMvcTest(CalendarEventController.class)
class CalendarEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CalendarEventService calendarEventService;

    @Autowired
    private ObjectMapper objectMapper;

    private CalendarEventRequest testRequest;
    private CalendarEventResponse testResponse;

    @BeforeEach
    void setUp() {
        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        LocalDateTime endTime = startTime.plusHours(2);

        testRequest = new CalendarEventRequest(
            "테스트 이벤트",
            "테스트 설명",
            startTime,
            endTime,
            "테스트 장소",
            1L,
            Arrays.asList(30, 60)
        );

        testResponse = new CalendarEventResponse();
        testResponse.setId(1L);
        testResponse.setTitle("테스트 이벤트");
        testResponse.setDescription("테스트 설명");
        testResponse.setStartTime(startTime);
        testResponse.setEndTime(endTime);
        testResponse.setLocation("테스트 장소");
        testResponse.setStatus(EventStatus.SCHEDULED);
        testResponse.setOwnerUsername("testuser");
        testResponse.setShared(false);
        testResponse.setCanEdit(true);
    }

    @Test
    @DisplayName("이벤트 생성 성공 테스트")
    @WithMockUser(username = "testuser")
    void createEventSuccess() throws Exception {
        // given
        when(calendarEventService.createEvent(eq("testuser"), any(CalendarEventRequest.class)))
            .thenReturn(testResponse);

        // when & then
        mockMvc.perform(post("/api/calendar/events")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("테스트 이벤트"))
                .andExpect(jsonPath("$.ownerUsername").value("testuser"));

        verify(calendarEventService).createEvent(eq("testuser"), any(CalendarEventRequest.class));
    }

    @Test
    @DisplayName("유효하지 않은 데이터로 이벤트 생성 시 400 에러")
    @WithMockUser(username = "testuser")
    void createEventWithInvalidData() throws Exception {
        // given
        CalendarEventRequest invalidRequest = new CalendarEventRequest();
        invalidRequest.setTitle(""); // 빈 제목
        invalidRequest.setStartTime(null); // null 시작 시간

        // when & then
        mockMvc.perform(post("/api/calendar/events")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(calendarEventService, never()).createEvent(anyString(), any(CalendarEventRequest.class));
    }

    @Test
    @DisplayName("이벤트 수정 성공 테스트")
    @WithMockUser(username = "testuser")
    void updateEventSuccess() throws Exception {
        // given
        when(calendarEventService.updateEvent(eq("testuser"), eq(1L), any(CalendarEventRequest.class)))
            .thenReturn(testResponse);

        // when & then
        mockMvc.perform(put("/api/calendar/events/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("테스트 이벤트"));

        verify(calendarEventService).updateEvent(eq("testuser"), eq(1L), any(CalendarEventRequest.class));
    }

    @Test
    @DisplayName("이벤트 삭제 성공 테스트")
    @WithMockUser(username = "testuser")
    void deleteEventSuccess() throws Exception {
        // given
        doNothing().when(calendarEventService).deleteEvent("testuser", 1L);

        // when & then
        mockMvc.perform(delete("/api/calendar/events/1")
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(calendarEventService).deleteEvent("testuser", 1L);
    }

    @Test
    @DisplayName("이벤트 조회 성공 테스트")
    @WithMockUser(username = "testuser")
    void getEventSuccess() throws Exception {
        // given
        when(calendarEventService.getEvent("testuser", 1L)).thenReturn(testResponse);

        // when & then
        mockMvc.perform(get("/api/calendar/events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("테스트 이벤트"));

        verify(calendarEventService).getEvent("testuser", 1L);
    }

    @Test
    @DisplayName("사용자 이벤트 목록 조회 테스트")
    @WithMockUser(username = "testuser")
    void getUserEventsSuccess() throws Exception {
        // given
        List<CalendarEventResponse> responses = Arrays.asList(testResponse);
        when(calendarEventService.getUserEvents("testuser")).thenReturn(responses);

        // when & then
        mockMvc.perform(get("/api/calendar/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].title").value("테스트 이벤트"));

        verify(calendarEventService).getUserEvents("testuser");
    }

    @Test
    @DisplayName("날짜 범위별 이벤트 조회 테스트")
    @WithMockUser(username = "testuser")
    void getEventsByDateRangeSuccess() throws Exception {
        // given
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(7);
        List<CalendarEventResponse> responses = Arrays.asList(testResponse);
        
        when(calendarEventService.getUserEvents("testuser", startDate, endDate)).thenReturn(responses);

        // when & then
        mockMvc.perform(get("/api/calendar/events/range")
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(calendarEventService).getUserEvents("testuser", startDate, endDate);
    }

    @Test
    @DisplayName("월별 이벤트 조회 테스트")
    @WithMockUser(username = "testuser")
    void getMonthlyEventsSuccess() throws Exception {
        // given
        List<CalendarEventResponse> responses = Arrays.asList(testResponse);
        when(calendarEventService.getMonthlyEvents("testuser", 2024, 12)).thenReturn(responses);

        // when & then
        mockMvc.perform(get("/api/calendar/events/month/2024/12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(calendarEventService).getMonthlyEvents("testuser", 2024, 12);
    }

    @Test
    @DisplayName("주별 이벤트 조회 테스트")
    @WithMockUser(username = "testuser")
    void getWeeklyEventsSuccess() throws Exception {
        // given
        List<CalendarEventResponse> responses = Arrays.asList(testResponse);
        when(calendarEventService.getWeeklyEvents("testuser", 2024, 50)).thenReturn(responses);

        // when & then
        mockMvc.perform(get("/api/calendar/events/week/2024/50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(calendarEventService).getWeeklyEvents("testuser", 2024, 50);
    }

    @Test
    @DisplayName("일별 이벤트 조회 테스트")
    @WithMockUser(username = "testuser")
    void getDailyEventsSuccess() throws Exception {
        // given
        LocalDate date = LocalDate.now();
        List<CalendarEventResponse> responses = Arrays.asList(testResponse);
        when(calendarEventService.getDailyEvents("testuser", date)).thenReturn(responses);

        // when & then
        mockMvc.perform(get("/api/calendar/events/daily/" + date.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(calendarEventService).getDailyEvents("testuser", date);
    }

    @Test
    @DisplayName("이벤트 상태 변경 테스트")
    @WithMockUser(username = "testuser")
    void updateEventStatusSuccess() throws Exception {
        // given
        testResponse.setStatus(EventStatus.COMPLETED);
        when(calendarEventService.updateEventStatus("testuser", 1L, EventStatus.COMPLETED))
            .thenReturn(testResponse);

        // when & then
        mockMvc.perform(patch("/api/calendar/events/1/status")
                .with(csrf())
                .param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        verify(calendarEventService).updateEventStatus("testuser", 1L, EventStatus.COMPLETED);
    }

    @Test
    @DisplayName("카테고리별 이벤트 조회 테스트")
    @WithMockUser(username = "testuser")
    void getEventsByCategorySuccess() throws Exception {
        // given
        List<CalendarEventResponse> responses = Arrays.asList(testResponse);
        when(calendarEventService.getEventsByCategory("testuser", 1L)).thenReturn(responses);

        // when & then
        mockMvc.perform(get("/api/calendar/events/category/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(calendarEventService).getEventsByCategory("testuser", 1L);
    }

    @Test
    @DisplayName("겹치는 이벤트 조회 테스트")
    @WithMockUser(username = "testuser")
    void getOverlappingEventsSuccess() throws Exception {
        // given
        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        LocalDateTime endTime = startTime.plusHours(2);
        List<CalendarEventResponse> responses = Arrays.asList(testResponse);
        
        when(calendarEventService.getOverlappingEvents("testuser", startTime, endTime, null))
            .thenReturn(responses);

        // when & then
        mockMvc.perform(get("/api/calendar/events/overlapping")
                .param("startTime", startTime.toString())
                .param("endTime", endTime.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(calendarEventService).getOverlappingEvents("testuser", startTime, endTime, null);
    }

    @Test
    @DisplayName("공유된 이벤트 조회 테스트")
    @WithMockUser(username = "testuser")
    void getSharedEventsSuccess() throws Exception {
        // given
        List<CalendarEventResponse> responses = Arrays.asList(testResponse);
        when(calendarEventService.getSharedEvents("testuser")).thenReturn(responses);

        // when & then
        mockMvc.perform(get("/api/calendar/events/shared"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(calendarEventService).getSharedEvents("testuser");
    }

    @Test
    @DisplayName("공유된 특정 이벤트 조회 테스트")
    @WithMockUser(username = "testuser")
    void getSharedEventSuccess() throws Exception {
        // given
        when(calendarEventService.getSharedEvent("testuser", 1L)).thenReturn(testResponse);

        // when & then
        mockMvc.perform(get("/api/calendar/events/shared/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("테스트 이벤트"));

        verify(calendarEventService).getSharedEvent("testuser", 1L);
    }

    @Test
    @DisplayName("인증되지 않은 사용자 접근 시 401 에러")
    void accessWithoutAuthentication() throws Exception {
        // when & then
        mockMvc.perform(get("/api/calendar/events"))
                .andExpect(status().isUnauthorized());

        verify(calendarEventService, never()).getUserEvents(anyString());
    }
}