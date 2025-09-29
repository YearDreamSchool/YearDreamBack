package elice.yeardreamback.calender.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import elice.yeardreamback.calender.dto.CalendarEventRequest;
import elice.yeardreamback.calender.dto.EventCategoryRequest;
import elice.yeardreamback.calender.entity.CalendarEvent;
import elice.yeardreamback.calender.entity.EventCategory;
import elice.yeardreamback.calender.enums.EventStatus;
import elice.yeardreamback.calender.repository.CalendarEventRepository;
import elice.yeardreamback.calender.repository.EventCategoryRepository;
import elice.yeardreamback.entity.User;
import elice.yeardreamback.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 캘린더 API 통합 테스트
 * 전체 요청/응답 흐름과 실제 시나리오를 테스트합니다.
 */
@SpringBootTest
@AutoConfigureWebMvc
@Transactional
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName("캘린더 API 통합 테스트")
class CalendarApiIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CalendarEventRepository calendarEventRepository;

    @Autowired
    private EventCategoryRepository eventCategoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private User testUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        // 테스트 사용자 생성
        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .build();
        userRepository.save(testUser);
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("전체 워크플로우 테스트 - 카테고리 생성 → 이벤트 생성 → 조회 → 수정 → 삭제")
    void fullWorkflowTest() throws Exception {
        // 1. 카테고리 생성
        EventCategoryRequest categoryRequest = EventCategoryRequest.builder()
                .name("업무")
                .color("#FF0000")
                .description("업무 관련 일정")
                .build();

        MvcResult categoryResult = mockMvc.perform(post("/api/calendar/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoryRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("업무"))
                .andExpect(jsonPath("$.color").value("#FF0000"))
                .andReturn();

        String categoryResponse = categoryResult.getResponse().getContentAsString();
        Long categoryId = objectMapper.readTree(categoryResponse).get("id").asLong();

        // 2. 이벤트 생성
        CalendarEventRequest eventRequest = CalendarEventRequest.builder()
                .title("팀 회의")
                .description("주간 팀 회의")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .location("회의실 A")
                .categoryId(categoryId)
                .reminderMinutes(Arrays.asList(30, 60))
                .build();

        MvcResult eventResult = mockMvc.perform(post("/api/calendar/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(eventRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("팀 회의"))
                .andExpect(jsonPath("$.description").value("주간 팀 회의"))
                .andExpect(jsonPath("$.location").value("회의실 A"))
                .andExpect(jsonPath("$.category.id").value(categoryId))
                .andExpect(jsonPath("$.status").value("SCHEDULED"))
                .andExpect(jsonPath("$.ownerUsername").value("testuser"))
                .andReturn();

        String eventResponse = eventResult.getResponse().getContentAsString();
        Long eventId = objectMapper.readTree(eventResponse).get("id").asLong();

        // 3. 이벤트 조회
        mockMvc.perform(get("/api/calendar/events/{eventId}", eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(eventId))
                .andExpect(jsonPath("$.title").value("팀 회의"))
                .andExpect(jsonPath("$.category.name").value("업무"));

        // 4. 이벤트 수정
        CalendarEventRequest updateRequest = CalendarEventRequest.builder()
                .title("팀 회의 (수정됨)")
                .description("주간 팀 회의 - 프로젝트 리뷰 포함")
                .startTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .location("회의실 B")
                .categoryId(categoryId)
                .reminderMinutes(Arrays.asList(15, 30))
                .build();

        mockMvc.perform(put("/api/calendar/events/{eventId}", eventId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("팀 회의 (수정됨)"))
                .andExpect(jsonPath("$.description").value("주간 팀 회의 - 프로젝트 리뷰 포함"))
                .andExpect(jsonPath("$.location").value("회의실 B"));

        // 5. 이벤트 상태 변경
        mockMvc.perform(patch("/api/calendar/events/{eventId}/status", eventId)
                .param("status", "IN_PROGRESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        // 6. 사용자 이벤트 목록 조회
        mockMvc.perform(get("/api/calendar/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(eventId));

        // 7. 카테고리별 이벤트 조회
        mockMvc.perform(get("/api/calendar/events/category/{categoryId}", categoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].category.id").value(categoryId));

        // 8. 이벤트 삭제
        mockMvc.perform(delete("/api/calendar/events/{eventId}", eventId))
                .andExpect(status().isNoContent());

        // 9. 삭제된 이벤트 조회 시도 (404 예상)
        mockMvc.perform(get("/api/calendar/events/{eventId}", eventId))
                .andExpect(status().isNotFound());

        // 10. 카테고리 삭제 (이벤트가 삭제되었으므로 가능)
        mockMvc.perform(delete("/api/calendar/categories/{categoryId}", categoryId))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("날짜 범위별 이벤트 조회 테스트")
    void dateRangeEventQueryTest() throws Exception {
        // 테스트 데이터 생성
        LocalDateTime baseTime = LocalDateTime.of(2024, 12, 15, 10, 0);
        
        // 여러 이벤트 생성
        createTestEvent("이벤트 1", baseTime, baseTime.plusHours(1));
        createTestEvent("이벤트 2", baseTime.plusDays(1), baseTime.plusDays(1).plusHours(1));
        createTestEvent("이벤트 3", baseTime.plusDays(7), baseTime.plusDays(7).plusHours(1));
        createTestEvent("이벤트 4", baseTime.plusDays(15), baseTime.plusDays(15).plusHours(1));

        // 날짜 범위별 조회
        mockMvc.perform(get("/api/calendar/events/range")
                .param("startDate", "2024-12-15")
                .param("endDate", "2024-12-16"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2)); // 이벤트 1, 2

        // 월별 조회
        mockMvc.perform(get("/api/calendar/events/month/2024/12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(4)); // 모든 이벤트

        // 일별 조회
        mockMvc.perform(get("/api/calendar/events/daily/2024-12-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1)); // 이벤트 1만
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("겹치는 이벤트 확인 테스트")
    void overlappingEventsTest() throws Exception {
        // 기존 이벤트 생성
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        LocalDateTime endTime = startTime.plusHours(2);
        
        Long eventId = createTestEvent("기존 이벤트", startTime, endTime);

        // 겹치는 시간대 확인
        mockMvc.perform(get("/api/calendar/events/overlapping")
                .param("startTime", startTime.plusMinutes(30).toString())
                .param("endTime", endTime.plusMinutes(30).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(eventId));

        // 겹치지 않는 시간대 확인
        mockMvc.perform(get("/api/calendar/events/overlapping")
                .param("startTime", endTime.plusHours(1).toString())
                .param("endTime", endTime.plusHours(2).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        // 현재 이벤트 제외하고 확인
        mockMvc.perform(get("/api/calendar/events/overlapping")
                .param("startTime", startTime.toString())
                .param("endTime", endTime.toString())
                .param("excludeEventId", eventId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("카테고리 관리 전체 시나리오 테스트")
    void categoryManagementTest() throws Exception {
        // 1. 카테고리 생성
        EventCategoryRequest request1 = EventCategoryRequest.builder()
                .name("업무")
                .color("#FF0000")
                .description("업무 관련")
                .build();

        MvcResult result1 = mockMvc.perform(post("/api/calendar/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated())
                .andReturn();

        Long categoryId1 = objectMapper.readTree(result1.getResponse().getContentAsString())
                .get("id").asLong();

        // 2. 두 번째 카테고리 생성
        EventCategoryRequest request2 = EventCategoryRequest.builder()
                .name("개인")
                .color("#00FF00")
                .description("개인 일정")
                .build();

        MvcResult result2 = mockMvc.perform(post("/api/calendar/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated())
                .andReturn();

        Long categoryId2 = objectMapper.readTree(result2.getResponse().getContentAsString())
                .get("id").asLong();

        // 3. 카테고리 목록 조회
        mockMvc.perform(get("/api/calendar/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));

        // 4. 특정 카테고리 조회
        mockMvc.perform(get("/api/calendar/categories/{categoryId}", categoryId1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("업무"))
                .andExpect(jsonPath("$.color").value("#FF0000"));

        // 5. 카테고리 수정
        EventCategoryRequest updateRequest = EventCategoryRequest.builder()
                .name("업무 (수정됨)")
                .color("#FF5500")
                .description("업무 관련 일정 (수정됨)")
                .build();

        mockMvc.perform(put("/api/calendar/categories/{categoryId}", categoryId1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("업무 (수정됨)"))
                .andExpect(jsonPath("$.color").value("#FF5500"));

        // 6. 카테고리에 이벤트 추가
        createTestEventWithCategory("테스트 이벤트", categoryId1);

        // 7. 이벤트가 있는 카테고리 조회
        mockMvc.perform(get("/api/calendar/categories/with-events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(categoryId1));

        // 8. 이벤트가 없는 카테고리 조회
        mockMvc.perform(get("/api/calendar/categories/without-events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(categoryId2));

        // 9. 이벤트가 있는 카테고리 삭제 시도 (실패 예상)
        mockMvc.perform(delete("/api/calendar/categories/{categoryId}", categoryId1))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").containsString("이벤트가 있는 카테고리는 삭제할 수 없습니다"));

        // 10. 이벤트가 없는 카테고리 삭제 (성공)
        mockMvc.perform(delete("/api/calendar/categories/{categoryId}", categoryId2))
                .andExpect(status().isNoContent());

        // 11. 카테고리 이름 중복 확인
        mockMvc.perform(get("/api/calendar/categories/check-duplicate")
                .param("name", "업무 (수정됨)"))
                .andExpect(status().isOk())
                .andExpect(content().string("true")); // 중복됨

        mockMvc.perform(get("/api/calendar/categories/check-duplicate")
                .param("name", "새로운 카테고리"))
                .andExpect(status().isOk())
                .andExpect(content().string("false")); // 중복되지 않음

        // 12. 사용자 카테고리 총 개수 조회
        mockMvc.perform(get("/api/calendar/categories/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("1")); // 1개 남음
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("유효성 검사 테스트")
    void validationTest() throws Exception {
        // 1. 빈 제목으로 이벤트 생성 시도
        CalendarEventRequest invalidRequest = CalendarEventRequest.builder()
                .title("") // 빈 제목
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .build();

        mockMvc.perform(post("/api/calendar/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.fieldErrors.title").exists());

        // 2. 잘못된 시간 범위로 이벤트 생성 시도
        CalendarEventRequest invalidTimeRequest = CalendarEventRequest.builder()
                .title("테스트 이벤트")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now()) // 종료 시간이 시작 시간보다 이전
                .build();

        mockMvc.perform(post("/api/calendar/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidTimeRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").containsString("시간"));

        // 3. 잘못된 색상 코드로 카테고리 생성 시도
        EventCategoryRequest invalidColorRequest = EventCategoryRequest.builder()
                .name("테스트 카테고리")
                .color("invalid-color") // 잘못된 색상 형식
                .build();

        mockMvc.perform(post("/api/calendar/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidColorRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.fieldErrors.color").exists());
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("에러 시나리오 테스트")
    void errorScenarioTest() throws Exception {
        // 1. 존재하지 않는 이벤트 조회
        mockMvc.perform(get("/api/calendar/events/{eventId}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Event Not Found"));

        // 2. 존재하지 않는 카테고리 조회
        mockMvc.perform(get("/api/calendar/categories/{categoryId}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Category Not Found"));

        // 3. 존재하지 않는 카테고리로 이벤트 생성 시도
        CalendarEventRequest requestWithInvalidCategory = CalendarEventRequest.builder()
                .title("테스트 이벤트")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .categoryId(999L) // 존재하지 않는 카테고리
                .build();

        mockMvc.perform(post("/api/calendar/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestWithInvalidCategory)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Category Not Found"));
    }

    // 헬퍼 메서드들
    private Long createTestEvent(String title, LocalDateTime startTime, LocalDateTime endTime) throws Exception {
        CalendarEventRequest request = CalendarEventRequest.builder()
                .title(title)
                .description("테스트 설명")
                .startTime(startTime)
                .endTime(endTime)
                .build();

        MvcResult result = mockMvc.perform(post("/api/calendar/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asLong();
    }

    private Long createTestEventWithCategory(String title, Long categoryId) throws Exception {
        CalendarEventRequest request = CalendarEventRequest.builder()
                .title(title)
                .description("테스트 설명")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .categoryId(categoryId)
                .build();

        MvcResult result = mockMvc.perform(post("/api/calendar/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asLong();
    }
}