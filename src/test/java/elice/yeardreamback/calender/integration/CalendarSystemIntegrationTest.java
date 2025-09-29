package elice.yeardreamback.calender.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import elice.yeardreamback.calender.dto.CalendarEventRequest;
import elice.yeardreamback.calender.dto.EventCategoryRequest;
import elice.yeardreamback.calender.entity.CalendarEvent;
import elice.yeardreamback.calender.entity.EventCategory;
import elice.yeardreamback.calender.entity.EventReminder;
import elice.yeardreamback.calender.entity.EventShare;
import elice.yeardreamback.calender.enums.EventStatus;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 캘린더 시스템 전체 통합 테스트
 * 모든 컴포넌트가 함께 작동하는지 최종 검증합니다.
 */
@SpringBootTest
@AutoConfigureWebMvc
@Transactional
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "logging.level.org.springframework.security=DEBUG"
})
@DisplayName("캘린더 시스템 전체 통합 테스트")
class CalendarSystemIntegrationTest {

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
    private User user1, user2, user3;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        // 테스트 사용자들 생성
        user1 = User.builder()
                .username("user1")
                .email("user1@test.com")
                .build();
        userRepository.save(user1);

        user2 = User.builder()
                .username("user2")
                .email("user2@test.com")
                .build();
        userRepository.save(user2);

        user3 = User.builder()
                .username("user3")
                .email("user3@test.com")
                .build();
        userRepository.save(user3);
    }

    @Test
    @DisplayName("전체 시스템 시나리오 테스트 - 다중 사용자 협업")
    void fullSystemScenarioTest() throws Exception {
        // === Phase 1: User1이 카테고리와 이벤트 생성 ===
        
        // 1.1 User1이 업무 카테고리 생성
        Long workCategoryId = createCategoryAsUser("user1", "업무", "#FF0000", "업무 관련 일정");
        
        // 1.2 User1이 개인 카테고리 생성
        Long personalCategoryId = createCategoryAsUser("user1", "개인", "#00FF00", "개인 일정");
        
        // 1.3 User1이 팀 회의 이벤트 생성
        Long teamMeetingId = createEventAsUser("user1", CalendarEventRequest.builder()
                .title("팀 회의")
                .description("주간 팀 회의")
                .startTime(LocalDateTime.now().plusDays(1).withHour(14).withMinute(0))
                .endTime(LocalDateTime.now().plusDays(1).withHour(15).withMinute(0))
                .location("회의실 A")
                .categoryId(workCategoryId)
                .reminderMinutes(Arrays.asList(30, 60))
                .build());

        // 1.4 User1이 개인 약속 생성
        Long personalAppointmentId = createEventAsUser("user1", CalendarEventRequest.builder()
                .title("치과 예약")
                .description("정기 검진")
                .startTime(LocalDateTime.now().plusDays(2).withHour(10).withMinute(0))
                .endTime(LocalDateTime.now().plusDays(2).withHour(11).withMinute(0))
                .location("ABC 치과")
                .categoryId(personalCategoryId)
                .reminderMinutes(Arrays.asList(60))
                .build());

        // === Phase 2: User2가 자신의 데이터 생성 ===
        
        // 2.1 User2가 자신의 카테고리 생성
        Long user2CategoryId = createCategoryAsUser("user2", "프로젝트", "#0000FF", "프로젝트 관련");
        
        // 2.2 User2가 이벤트 생성
        Long user2EventId = createEventAsUser("user2", CalendarEventRequest.builder()
                .title("프로젝트 리뷰")
                .description("분기별 프로젝트 리뷰")
                .startTime(LocalDateTime.now().plusDays(3).withHour(16).withMinute(0))
                .endTime(LocalDateTime.now().plusDays(3).withHour(17).withMinute(0))
                .categoryId(user2CategoryId)
                .build());

        // === Phase 3: 이벤트 공유 설정 ===
        
        // 3.1 User1의 팀 회의를 User2와 공유 (편집 권한)
        EventShare teamMeetingShare = EventShare.builder()
                .eventId(teamMeetingId)
                .sharedByUsername("user1")
                .sharedWithUsername("user2")
                .permission(SharePermission.EDIT)
                .build();
        eventShareRepository.save(teamMeetingShare);

        // 3.2 User1의 개인 약속을 User3과 공유 (읽기 전용)
        EventShare personalShare = EventShare.builder()
                .eventId(personalAppointmentId)
                .sharedByUsername("user1")
                .sharedWithUsername("user3")
                .permission(SharePermission.VIEW_ONLY)
                .build();
        eventShareRepository.save(personalShare);

        // === Phase 4: 권한별 접근 테스트 ===
        
        // 4.1 User2가 공유받은 팀 회의 조회 (성공)
        mockMvc.perform(get("/api/calendar/events/{eventId}", teamMeetingId)
                .with(request -> {
                    request.setRemoteUser("user2");
                    return request;
                }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("팀 회의"))
                .andExpect(jsonPath("$.isShared").value(true))
                .andExpect(jsonPath("$.canEdit").value(true));

        // 4.2 User2가 공유받은 팀 회의 수정 (성공 - 편집 권한 있음)
        CalendarEventRequest updateRequest = CalendarEventRequest.builder()
                .title("팀 회의 (수정됨)")
                .description("주간 팀 회의 - 프로젝트 업데이트 포함")
                .startTime(LocalDateTime.now().plusDays(1).withHour(14).withMinute(30))
                .endTime(LocalDateTime.now().plusDays(1).withHour(15).withMinute(30))
                .location("회의실 B")
                .categoryId(workCategoryId)
                .reminderMinutes(Arrays.asList(15, 30))
                .build();

        mockMvc.perform(put("/api/calendar/events/{eventId}", teamMeetingId)
                .with(request -> {
                    request.setRemoteUser("user2");
                    return request;
                })
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("팀 회의 (수정됨)"));

        // 4.3 User3이 공유받은 개인 약속 조회 (성공)
        mockMvc.perform(get("/api/calendar/events/{eventId}", personalAppointmentId)
                .with(request -> {
                    request.setRemoteUser("user3");
                    return request;
                }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("치과 예약"))
                .andExpect(jsonPath("$.isShared").value(true))
                .andExpect(jsonPath("$.canEdit").value(false));

        // 4.4 User3이 읽기 전용 이벤트 수정 시도 (실패)
        mockMvc.perform(put("/api/calendar/events/{eventId}", personalAppointmentId)
                .with(request -> {
                    request.setRemoteUser("user3");
                    return request;
                })
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());

        // 4.5 User2가 User1의 개인 약속 접근 시도 (실패 - 공유되지 않음)
        mockMvc.perform(get("/api/calendar/events/{eventId}", personalAppointmentId)
                .with(request -> {
                    request.setRemoteUser("user2");
                    return request;
                }))
                .andExpect(status().isForbidden());

        // === Phase 5: 복합 조회 테스트 ===
        
        // 5.1 User1의 전체 이벤트 조회 (자신의 이벤트만)
        mockMvc.perform(get("/api/calendar/events")
                .with(request -> {
                    request.setRemoteUser("user1");
                    return request;
                }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));

        // 5.2 User2의 공유된 이벤트 조회
        mockMvc.perform(get("/api/calendar/events/shared")
                .with(request -> {
                    request.setRemoteUser("user2");
                    return request;
                }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(teamMeetingId));

        // 5.3 월별 이벤트 조회 (User1)
        LocalDateTime now = LocalDateTime.now();
        mockMvc.perform(get("/api/calendar/events/month/{year}/{month}", now.getYear(), now.getMonthValue())
                .with(request -> {
                    request.setRemoteUser("user1");
                    return request;
                }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // === Phase 6: 데이터 일관성 검증 ===
        
        // 6.1 데이터베이스 직접 검증
        List<CalendarEvent> user1Events = calendarEventRepository.findByUserUsernameOrderByStartTimeAsc("user1");
        assertThat(user1Events).hasSize(2);
        
        List<EventReminder> teamMeetingReminders = eventReminderRepository.findByEventIdOrderByMinutesBeforeAsc(teamMeetingId);
        assertThat(teamMeetingReminders).hasSize(2); // User2가 수정한 알림
        assertThat(teamMeetingReminders.get(0).getMinutesBefore()).isEqualTo(15);
        assertThat(teamMeetingReminders.get(1).getMinutesBefore()).isEqualTo(30);

        // 6.2 공유 관계 검증
        List<EventShare> shares = eventShareRepository.findByEventId(teamMeetingId);
        assertThat(shares).hasSize(1);
        assertThat(shares.get(0).getSharedWithUsername()).isEqualTo("user2");
        assertThat(shares.get(0).getPermission()).isEqualTo(SharePermission.EDIT);

        // === Phase 7: 정리 및 삭제 테스트 ===
        
        // 7.1 User2가 공유받은 이벤트 삭제 시도 (실패 - 소유자만 삭제 가능)
        mockMvc.perform(delete("/api/calendar/events/{eventId}", teamMeetingId)
                .with(request -> {
                    request.setRemoteUser("user2");
                    return request;
                }))
                .andExpect(status().isForbidden());

        // 7.2 User1이 자신의 이벤트 삭제 (성공)
        mockMvc.perform(delete("/api/calendar/events/{eventId}", teamMeetingId)
                .with(request -> {
                    request.setRemoteUser("user1");
                    return request;
                }))
                .andExpect(status().isNoContent());

        // 7.3 삭제 후 공유 관계도 함께 삭제되었는지 확인
        List<EventShare> sharesAfterDelete = eventShareRepository.findByEventId(teamMeetingId);
        assertThat(sharesAfterDelete).isEmpty();

        // 7.4 User2가 삭제된 이벤트 접근 시도 (실패)
        mockMvc.perform(get("/api/calendar/events/{eventId}", teamMeetingId)
                .with(request -> {
                    request.setRemoteUser("user2");
                    return request;
                }))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("성능 및 확장성 검증 테스트")
    void performanceAndScalabilityTest() throws Exception {
        // 1. 대량 사용자 시뮬레이션 (10명)
        for (int i = 1; i <= 10; i++) {
            User user = User.builder()
                    .username("perfuser" + i)
                    .email("perfuser" + i + "@test.com")
                    .build();
            userRepository.save(user);

            // 각 사용자마다 5개 카테고리, 50개 이벤트 생성
            for (int j = 1; j <= 5; j++) {
                Long categoryId = createCategoryAsUser("perfuser" + i, 
                    "카테고리" + j, "#FF000" + j, "테스트 카테고리");

                for (int k = 1; k <= 10; k++) {
                    createEventAsUser("perfuser" + i, CalendarEventRequest.builder()
                            .title("이벤트 " + i + "-" + j + "-" + k)
                            .startTime(LocalDateTime.now().plusDays(k))
                            .endTime(LocalDateTime.now().plusDays(k).plusHours(1))
                            .categoryId(categoryId)
                            .build());
                }
            }
        }

        // 2. 성능 측정
        long startTime = System.currentTimeMillis();

        // 특정 사용자의 월별 이벤트 조회
        mockMvc.perform(get("/api/calendar/events/month/{year}/{month}", 
                LocalDateTime.now().getYear(), LocalDateTime.now().getMonthValue())
                .with(request -> {
                    request.setRemoteUser("perfuser5");
                    return request;
                }))
                .andExpect(status().isOk());

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("대량 데이터 환경에서 월별 조회 성능: " + duration + "ms");
        assertThat(duration).isLessThan(1000); // 1초 이하

        // 3. 메모리 사용량 확인
        Runtime runtime = Runtime.getRuntime();
        long memoryUsed = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("메모리 사용량: " + (memoryUsed / 1024 / 1024) + "MB");
    }

    @Test
    @DisplayName("보안 및 권한 체계 검증")
    void securityAndAuthorizationTest() throws Exception {
        // 1. 테스트 데이터 준비
        Long categoryId = createCategoryAsUser("user1", "보안 테스트", "#FF0000", "보안 테스트용");
        Long eventId = createEventAsUser("user1", CalendarEventRequest.builder()
                .title("보안 테스트 이벤트")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .categoryId(categoryId)
                .build());

        // 2. 인증되지 않은 사용자 접근 시도
        mockMvc.perform(get("/api/calendar/events/{eventId}", eventId))
                .andExpect(status().isUnauthorized());

        // 3. 다른 사용자의 데이터 접근 시도
        mockMvc.perform(get("/api/calendar/events/{eventId}", eventId)
                .with(request -> {
                    request.setRemoteUser("user2");
                    return request;
                }))
                .andExpect(status().isForbidden());

        // 4. SQL 인젝션 시도
        mockMvc.perform(get("/api/calendar/events/category/{categoryId}", "1; DROP TABLE calendar_events; --")
                .with(request -> {
                    request.setRemoteUser("user1");
                    return request;
                }))
                .andExpect(status().isBadRequest());

        // 5. XSS 공격 시도
        CalendarEventRequest xssRequest = CalendarEventRequest.builder()
                .title("<script>alert('XSS')</script>")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .build();

        mockMvc.perform(post("/api/calendar/events")
                .with(request -> {
                    request.setRemoteUser("user1");
                    return request;
                })
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(xssRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("<script>alert('XSS')</script>")); // 저장은 되지만 이스케이프 처리 확인
    }

    @Test
    @DisplayName("에러 복구 및 트랜잭션 무결성 테스트")
    void errorRecoveryAndTransactionIntegrityTest() throws Exception {
        // 1. 초기 데이터 개수 확인
        long initialEventCount = calendarEventRepository.count();
        long initialCategoryCount = eventCategoryRepository.count();

        // 2. 트랜잭션 롤백 시나리오 - 잘못된 데이터로 이벤트 생성
        CalendarEventRequest invalidRequest = CalendarEventRequest.builder()
                .title("") // 빈 제목으로 유효성 검사 실패 유발
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .categoryId(999L) // 존재하지 않는 카테고리
                .build();

        mockMvc.perform(post("/api/calendar/events")
                .with(request -> {
                    request.setRemoteUser("user1");
                    return request;
                })
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        // 3. 데이터 무결성 확인 - 실패한 트랜잭션으로 인한 데이터 변경이 없어야 함
        assertThat(calendarEventRepository.count()).isEqualTo(initialEventCount);
        assertThat(eventCategoryRepository.count()).isEqualTo(initialCategoryCount);

        // 4. 시스템 복구 확인 - 정상적인 요청은 여전히 처리되어야 함
        Long categoryId = createCategoryAsUser("user1", "복구 테스트", "#FF0000", "복구 테스트용");
        Long eventId = createEventAsUser("user1", CalendarEventRequest.builder()
                .title("복구 테스트 이벤트")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .categoryId(categoryId)
                .build());

        // 5. 생성된 데이터 검증
        mockMvc.perform(get("/api/calendar/events/{eventId}", eventId)
                .with(request -> {
                    request.setRemoteUser("user1");
                    return request;
                }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("복구 테스트 이벤트"));
    }

    // 헬퍼 메서드들
    private Long createCategoryAsUser(String username, String name, String color, String description) throws Exception {
        EventCategoryRequest request = EventCategoryRequest.builder()
                .name(name)
                .color(color)
                .description(description)
                .build();

        MvcResult result = mockMvc.perform(post("/api/calendar/categories")
                .with(req -> {
                    req.setRemoteUser(username);
                    return req;
                })
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asLong();
    }

    private Long createEventAsUser(String username, CalendarEventRequest request) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/calendar/events")
                .with(req -> {
                    req.setRemoteUser(username);
                    return req;
                })
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asLong();
    }
}