package elice.yeardreamback.calender.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import elice.yeardreamback.calender.dto.CalendarEventRequest;
import elice.yeardreamback.calender.dto.EventCategoryRequest;
import elice.yeardreamback.calender.entity.CalendarEvent;
import elice.yeardreamback.calender.entity.EventCategory;
import elice.yeardreamback.calender.entity.EventReminder;
import elice.yeardreamback.calender.entity.EventShare;
import elice.yeardreamback.calender.enums.SharePermission;
import elice.yeardreamback.calender.repository.CalendarEventRepository;
import elice.yeardreamback.calender.repository.EventCategoryRepository;
import elice.yeardreamback.calender.repository.EventReminderRepository;
import elice.yeardreamback.calender.repository.EventShareRepository;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 캘린더 데이터 일관성 및 트랜잭션 테스트
 * 데이터 무결성, 트랜잭션 롤백, 관계 데이터 일관성을 테스트합니다.
 */
@SpringBootTest
@AutoConfigureWebMvc
@Transactional
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName("캘린더 데이터 일관성 테스트")
class CalendarDataConsistencyTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CalendarEventRepository calendarEventRepository;

    @Autowired
    private EventCategoryRepository eventCategoryRepository;

    @Autowired
    private EventReminderRepository eventReminderRepository;

    @Autowired
    private EventShareRepository eventShareRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private User testUser;
    private User otherUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        // 테스트 사용자들 생성
        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .build();
        userRepository.save(testUser);

        otherUser = User.builder()
                .username("otheruser")
                .email("other@example.com")
                .build();
        userRepository.save(otherUser);
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("이벤트-카테고리 관계 일관성 테스트")
    void eventCategoryRelationshipConsistencyTest() throws Exception {
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
                .andReturn();

        Long categoryId = objectMapper.readTree(categoryResult.getResponse().getContentAsString())
                .get("id").asLong();

        // 2. 카테고리와 연결된 이벤트 생성
        CalendarEventRequest eventRequest = CalendarEventRequest.builder()
                .title("팀 회의")
                .description("주간 팀 회의")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .categoryId(categoryId)
                .build();

        MvcResult eventResult = mockMvc.perform(post("/api/calendar/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(eventRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.category.id").value(categoryId))
                .andReturn();

        Long eventId = objectMapper.readTree(eventResult.getResponse().getContentAsString())
                .get("id").asLong();

        // 3. 데이터베이스에서 관계 확인
        Optional<CalendarEvent> savedEvent = calendarEventRepository.findById(eventId);
        assertThat(savedEvent).isPresent();
        assertThat(savedEvent.get().getCategory()).isNotNull();
        assertThat(savedEvent.get().getCategory().getId()).isEqualTo(categoryId);

        // 4. 카테고리 삭제 시도 (이벤트가 있으므로 실패해야 함)
        mockMvc.perform(delete("/api/calendar/categories/{categoryId}", categoryId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").containsString("이벤트가 있는 카테고리는 삭제할 수 없습니다"));

        // 5. 이벤트 삭제 후 카테고리 삭제 (성공해야 함)
        mockMvc.perform(delete("/api/calendar/events/{eventId}", eventId))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/calendar/categories/{categoryId}", categoryId))
                .andExpect(status().isNoContent());

        // 6. 삭제 확인
        assertThat(calendarEventRepository.findById(eventId)).isEmpty();
        assertThat(eventCategoryRepository.findById(categoryId)).isEmpty();
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("이벤트-알림 관계 일관성 테스트")
    void eventReminderRelationshipConsistencyTest() throws Exception {
        // 1. 알림이 포함된 이벤트 생성
        CalendarEventRequest eventRequest = CalendarEventRequest.builder()
                .title("알림 테스트 이벤트")
                .description("알림 테스트")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .reminderMinutes(Arrays.asList(30, 60, 120))
                .build();

        MvcResult eventResult = mockMvc.perform(post("/api/calendar/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(eventRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reminders").isArray())
                .andExpect(jsonPath("$.reminders.length()").value(3))
                .andReturn();

        Long eventId = objectMapper.readTree(eventResult.getResponse().getContentAsString())
                .get("id").asLong();

        // 2. 데이터베이스에서 알림 확인
        List<EventReminder> reminders = eventReminderRepository.findByEventIdOrderByMinutesBeforeAsc(eventId);
        assertThat(reminders).hasSize(3);
        assertThat(reminders.get(0).getMinutesBefore()).isEqualTo(30);
        assertThat(reminders.get(1).getMinutesBefore()).isEqualTo(60);
        assertThat(reminders.get(2).getMinutesBefore()).isEqualTo(120);

        // 3. 이벤트 수정 (알림 변경)
        CalendarEventRequest updateRequest = CalendarEventRequest.builder()
                .title("알림 테스트 이벤트 (수정)")
                .description("알림 테스트 (수정)")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .reminderMinutes(Arrays.asList(15, 45)) // 알림 변경
                .build();

        mockMvc.perform(put("/api/calendar/events/{eventId}", eventId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reminders.length()").value(2));

        // 4. 변경된 알림 확인
        List<EventReminder> updatedReminders = eventReminderRepository.findByEventIdOrderByMinutesBeforeAsc(eventId);
        assertThat(updatedReminders).hasSize(2);
        assertThat(updatedReminders.get(0).getMinutesBefore()).isEqualTo(15);
        assertThat(updatedReminders.get(1).getMinutesBefore()).isEqualTo(45);

        // 5. 이벤트 삭제 시 알림도 함께 삭제되는지 확인
        mockMvc.perform(delete("/api/calendar/events/{eventId}", eventId))
                .andExpect(status().isNoContent());

        List<EventReminder> remindersAfterDelete = eventReminderRepository.findByEventIdOrderByMinutesBeforeAsc(eventId);
        assertThat(remindersAfterDelete).isEmpty();
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("이벤트 공유 관계 일관성 테스트")
    void eventSharingRelationshipConsistencyTest() throws Exception {
        // 1. 이벤트 생성
        CalendarEventRequest eventRequest = CalendarEventRequest.builder()
                .title("공유 테스트 이벤트")
                .description("공유 테스트")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .build();

        MvcResult eventResult = mockMvc.perform(post("/api/calendar/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(eventRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Long eventId = objectMapper.readTree(eventResult.getResponse().getContentAsString())
                .get("id").asLong();

        // 2. 이벤트 공유 설정 (직접 데이터베이스에 추가)
        EventShare eventShare = EventShare.builder()
                .eventId(eventId)
                .sharedByUsername("testuser")
                .sharedWithUsername("otheruser")
                .permission(SharePermission.VIEW_ONLY)
                .build();
        eventShareRepository.save(eventShare);

        // 3. 공유받은 사용자로 이벤트 조회
        mockMvc.perform(get("/api/calendar/events/{eventId}", eventId)
                .with(request -> {
                    request.setRemoteUser("otheruser");
                    return request;
                }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(eventId))
                .andExpect(jsonPath("$.isShared").value(true))
                .andExpect(jsonPath("$.canEdit").value(false));

        // 4. 공유 권한 변경
        eventShare.setPermission(SharePermission.EDIT);
        eventShareRepository.save(eventShare);

        // 5. 편집 권한으로 변경된 것 확인
        mockMvc.perform(get("/api/calendar/events/{eventId}", eventId)
                .with(request -> {
                    request.setRemoteUser("otheruser");
                    return request;
                }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canEdit").value(true));

        // 6. 이벤트 삭제 시 공유 정보도 함께 삭제되는지 확인
        mockMvc.perform(delete("/api/calendar/events/{eventId}", eventId))
                .andExpect(status().isNoContent());

        List<EventShare> sharesAfterDelete = eventShareRepository.findByEventId(eventId);
        assertThat(sharesAfterDelete).isEmpty();
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("사용자별 데이터 격리 테스트")
    void userDataIsolationTest() throws Exception {
        // 1. testuser로 카테고리와 이벤트 생성
        EventCategoryRequest categoryRequest = EventCategoryRequest.builder()
                .name("testuser 카테고리")
                .color("#FF0000")
                .build();

        MvcResult categoryResult = mockMvc.perform(post("/api/calendar/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoryRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Long categoryId = objectMapper.readTree(categoryResult.getResponse().getContentAsString())
                .get("id").asLong();

        CalendarEventRequest eventRequest = CalendarEventRequest.builder()
                .title("testuser 이벤트")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .categoryId(categoryId)
                .build();

        MvcResult eventResult = mockMvc.perform(post("/api/calendar/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(eventRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Long eventId = objectMapper.readTree(eventResult.getResponse().getContentAsString())
                .get("id").asLong();

        // 2. otheruser로 전환하여 데이터 접근 시도
        // otheruser는 testuser의 데이터에 접근할 수 없어야 함
        mockMvc.perform(get("/api/calendar/events/{eventId}", eventId)
                .with(request -> {
                    request.setRemoteUser("otheruser");
                    return request;
                }))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/calendar/categories/{categoryId}", categoryId)
                .with(request -> {
                    request.setRemoteUser("otheruser");
                    return request;
                }))
                .andExpect(status().isForbidden());

        // 3. otheruser의 이벤트 목록은 비어있어야 함
        mockMvc.perform(get("/api/calendar/events")
                .with(request -> {
                    request.setRemoteUser("otheruser");
                    return request;
                }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        // 4. otheruser의 카테고리 목록도 비어있어야 함
        mockMvc.perform(get("/api/calendar/categories")
                .with(request -> {
                    request.setRemoteUser("otheruser");
                    return request;
                }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("중복 데이터 방지 테스트")
    void duplicateDataPreventionTest() throws Exception {
        // 1. 카테고리 생성
        EventCategoryRequest categoryRequest = EventCategoryRequest.builder()
                .name("중복 테스트 카테고리")
                .color("#FF0000")
                .build();

        mockMvc.perform(post("/api/calendar/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoryRequest)))
                .andExpect(status().isCreated());

        // 2. 같은 이름의 카테고리 생성 시도 (실패해야 함)
        mockMvc.perform(post("/api/calendar/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoryRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").containsString("이미 존재하는 카테고리 이름입니다"));

        // 3. 카테고리 이름 중복 확인 API 테스트
        mockMvc.perform(get("/api/calendar/categories/check-duplicate")
                .param("name", "중복 테스트 카테고리"))
                .andExpect(status().isOk())
                .andExpect(content().string("true")); // 중복됨

        mockMvc.perform(get("/api/calendar/categories/check-duplicate")
                .param("name", "새로운 카테고리"))
                .andExpect(status().isOk())
                .andExpect(content().string("false")); // 중복되지 않음
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("트랜잭션 롤백 테스트")
    void transactionRollbackTest() throws Exception {
        // 이 테스트는 실제 트랜잭션 롤백을 시뮬레이션하기 어려우므로
        // 데이터 일관성 관점에서 검증

        // 1. 초기 데이터 개수 확인
        long initialEventCount = calendarEventRepository.count();
        long initialCategoryCount = eventCategoryRepository.count();

        // 2. 잘못된 데이터로 이벤트 생성 시도 (실패해야 함)
        CalendarEventRequest invalidRequest = CalendarEventRequest.builder()
                .title("") // 빈 제목 (유효성 검사 실패)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .build();

        mockMvc.perform(post("/api/calendar/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        // 3. 데이터 개수가 변경되지 않았는지 확인
        assertThat(calendarEventRepository.count()).isEqualTo(initialEventCount);

        // 4. 존재하지 않는 카테고리로 이벤트 생성 시도
        CalendarEventRequest invalidCategoryRequest = CalendarEventRequest.builder()
                .title("유효한 제목")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .categoryId(999L) // 존재하지 않는 카테고리
                .build();

        mockMvc.perform(post("/api/calendar/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidCategoryRequest)))
                .andExpect(status().isNotFound());

        // 5. 여전히 데이터 개수가 변경되지 않았는지 확인
        assertThat(calendarEventRepository.count()).isEqualTo(initialEventCount);
        assertThat(eventCategoryRepository.count()).isEqualTo(initialCategoryCount);
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("대량 데이터 일관성 테스트")
    void bulkDataConsistencyTest() throws Exception {
        // 1. 카테고리 생성
        EventCategoryRequest categoryRequest = EventCategoryRequest.builder()
                .name("대량 테스트 카테고리")
                .color("#FF0000")
                .build();

        MvcResult categoryResult = mockMvc.perform(post("/api/calendar/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoryRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Long categoryId = objectMapper.readTree(categoryResult.getResponse().getContentAsString())
                .get("id").asLong();

        // 2. 100개의 이벤트 생성
        for (int i = 0; i < 100; i++) {
            CalendarEventRequest eventRequest = CalendarEventRequest.builder()
                    .title("대량 테스트 이벤트 " + i)
                    .startTime(LocalDateTime.now().plusDays(i % 30))
                    .endTime(LocalDateTime.now().plusDays(i % 30).plusHours(1))
                    .categoryId(categoryId)
                    .build();

            mockMvc.perform(post("/api/calendar/events")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(eventRequest)))
                    .andExpect(status().isCreated());
        }

        // 3. 데이터 일관성 확인
        List<CalendarEvent> events = calendarEventRepository.findByUserUsernameOrderByStartTimeAsc("testuser");
        assertThat(events).hasSize(100);

        // 4. 모든 이벤트가 올바른 카테고리와 연결되어 있는지 확인
        for (CalendarEvent event : events) {
            assertThat(event.getCategory()).isNotNull();
            assertThat(event.getCategory().getId()).isEqualTo(categoryId);
            assertThat(event.getOwnerUsername()).isEqualTo("testuser");
        }

        // 5. 카테고리의 이벤트 개수 확인
        Optional<EventCategory> category = eventCategoryRepository.findById(categoryId);
        assertThat(category).isPresent();
        assertThat(category.get().getEventCount()).isEqualTo(100);
    }
}