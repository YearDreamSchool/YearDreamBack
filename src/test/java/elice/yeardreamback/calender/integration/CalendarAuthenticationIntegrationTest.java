package elice.yeardreamback.calender.integration;

import elice.yeardreamback.calender.dto.CalendarEventRequest;
import elice.yeardreamback.calender.dto.CalendarEventResponse;
import elice.yeardreamback.calender.service.CalendarEventService;
import elice.yeardreamback.jwt.JWTUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 캘린더 API JWT 인증 통합 테스트
 */
@SpringBootTest
@AutoConfigureWebMvc
class CalendarAuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JWTUtil jwtUtil;

    @MockBean
    private CalendarEventService calendarEventService;

    @Autowired
    private ObjectMapper objectMapper;

    private String validToken;
    private String expiredToken;
    private CalendarEventRequest testRequest;
    private CalendarEventResponse testResponse;

    @BeforeEach
    void setUp() {
        // 유효한 JWT 토큰 생성
        validToken = jwtUtil.createJwt("testuser", "USER", 60 * 60 * 1000L); // 1시간

        // 만료된 JWT 토큰 생성 (음수 시간으로 즉시 만료)
        expiredToken = jwtUtil.createJwt("testuser", "USER", -1000L);

        // 테스트 데이터 설정
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
        testResponse.setOwnerUsername("testuser");
    }

    @Test
    @DisplayName("유효한 JWT 토큰으로 이벤트 생성 성공")
    void createEventWithValidToken() throws Exception {
        // given
        when(calendarEventService.createEvent(eq("testuser"), any(CalendarEventRequest.class)))
            .thenReturn(testResponse);

        // when & then
        mockMvc.perform(post("/api/calendar/events")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("테스트 이벤트"))
                .andExpect(jsonPath("$.ownerUsername").value("testuser"));
    }

    @Test
    @DisplayName("JWT 토큰 없이 이벤트 생성 시 401 에러")
    void createEventWithoutToken() throws Exception {
        // when & then
        mockMvc.perform(post("/api/calendar/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("잘못된 JWT 토큰으로 이벤트 생성 시 401 에러")
    void createEventWithInvalidToken() throws Exception {
        // when & then
        mockMvc.perform(post("/api/calendar/events")
                .header("Authorization", "Bearer invalid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("만료된 JWT 토큰으로 이벤트 생성 시 401 에러")
    void createEventWithExpiredToken() throws Exception {
        // when & then
        mockMvc.perform(post("/api/calendar/events")
                .header("Authorization", "Bearer " + expiredToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("유효한 JWT 토큰으로 이벤트 목록 조회 성공")
    void getUserEventsWithValidToken() throws Exception {
        // given
        when(calendarEventService.getUserEvents("testuser")).thenReturn(Arrays.asList(testResponse));

        // when & then
        mockMvc.perform(get("/api/calendar/events")
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].ownerUsername").value("testuser"));
    }

    @Test
    @DisplayName("JWT 토큰 없이 이벤트 목록 조회 시 401 에러")
    void getUserEventsWithoutToken() throws Exception {
        // when & then
        mockMvc.perform(get("/api/calendar/events"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("유효한 JWT 토큰으로 카테고리 생성 성공")
    void createCategoryWithValidToken() throws Exception {
        // when & then
        mockMvc.perform(post("/api/calendar/categories")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"업무\",\"color\":\"#FF0000\",\"description\":\"업무 관련\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("JWT 토큰 없이 카테고리 생성 시 401 에러")
    void createCategoryWithoutToken() throws Exception {
        // when & then
        mockMvc.perform(post("/api/calendar/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"업무\",\"color\":\"#FF0000\",\"description\":\"업무 관련\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("유효한 JWT 토큰으로 이벤트 공유 성공")
    void shareEventWithValidToken() throws Exception {
        // when & then
        mockMvc.perform(post("/api/calendar/events/1/share")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"sharedWithUsername\":\"otheruser\",\"permission\":\"VIEW_ONLY\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("JWT 토큰 없이 이벤트 공유 시 401 에러")
    void shareEventWithoutToken() throws Exception {
        // when & then
        mockMvc.perform(post("/api/calendar/events/1/share")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"sharedWithUsername\":\"otheruser\",\"permission\":\"VIEW_ONLY\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("Bearer 접두사 없는 토큰으로 요청 시 401 에러")
    void requestWithTokenWithoutBearerPrefix() throws Exception {
        // when & then
        mockMvc.perform(get("/api/calendar/events")
                .header("Authorization", validToken)) // Bearer 접두사 없음
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("빈 Authorization 헤더로 요청 시 401 에러")
    void requestWithEmptyAuthorizationHeader() throws Exception {
        // when & then
        mockMvc.perform(get("/api/calendar/events")
                .header("Authorization", ""))
                .andExpect(status().isUnauthorized());
    }
}