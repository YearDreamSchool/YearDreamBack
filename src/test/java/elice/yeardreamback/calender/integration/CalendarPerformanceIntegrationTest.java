package elice.yeardreamback.calender.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import elice.yeardreamback.calender.dto.CalendarEventRequest;
import elice.yeardreamback.calender.dto.EventCategoryRequest;
import elice.yeardreamback.calender.entity.CalendarEvent;
import elice.yeardreamback.calender.entity.EventCategory;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 캘린더 API 성능 통합 테스트
 * 대량 데이터 처리 및 성능 관련 시나리오를 테스트합니다.
 */
@SpringBootTest
@AutoConfigureWebMvc
@Transactional
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "logging.level.org.hibernate.SQL=DEBUG"
})
@DisplayName("캘린더 API 성능 통합 테스트")
class CalendarPerformanceIntegrationTest {

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
    private EventCategory testCategory;

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

        // 테스트 카테고리 생성
        testCategory = EventCategory.builder()
                .name("테스트 카테고리")
                .color("#FF0000")
                .description("성능 테스트용 카테고리")
                .ownerUsername("testuser")
                .build();
        eventCategoryRepository.save(testCategory);
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("대량 이벤트 생성 성능 테스트")
    void bulkEventCreationPerformanceTest() throws Exception {
        int eventCount = 100;
        long startTime = System.currentTimeMillis();

        // 100개의 이벤트 생성
        for (int i = 0; i < eventCount; i++) {
            CalendarEventRequest request = CalendarEventRequest.builder()
                    .title("성능 테스트 이벤트 " + i)
                    .description("성능 테스트용 이벤트 설명 " + i)
                    .startTime(LocalDateTime.now().plusDays(i % 30))
                    .endTime(LocalDateTime.now().plusDays(i % 30).plusHours(1))
                    .categoryId(testCategory.getId())
                    .build();

            mockMvc.perform(post("/api/calendar/events")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("대량 이벤트 생성 소요 시간: " + duration + "ms");
        System.out.println("이벤트당 평균 생성 시간: " + (duration / eventCount) + "ms");

        // 성능 검증 (예: 이벤트당 100ms 이하)
        assertThat(duration / eventCount).isLessThan(100);

        // 데이터 검증
        List<CalendarEvent> events = calendarEventRepository.findByUserUsernameOrderByStartTimeAsc("testuser");
        assertThat(events).hasSize(eventCount);
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("대량 이벤트 조회 성능 테스트")
    void bulkEventQueryPerformanceTest() throws Exception {
        // 테스트 데이터 준비 (500개 이벤트)
        createBulkTestEvents(500);

        long startTime = System.currentTimeMillis();

        // 전체 이벤트 조회
        mockMvc.perform(get("/api/calendar/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(500));

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("500개 이벤트 조회 소요 시간: " + duration + "ms");

        // 성능 검증 (예: 1초 이하)
        assertThat(duration).isLessThan(1000);
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("월별 이벤트 조회 성능 테스트")
    void monthlyEventQueryPerformanceTest() throws Exception {
        // 1년치 이벤트 데이터 생성 (매일 2개씩, 총 730개)
        LocalDateTime baseDate = LocalDateTime.of(2024, 1, 1, 10, 0);
        for (int day = 0; day < 365; day++) {
            for (int event = 0; event < 2; event++) {
                CalendarEvent calendarEvent = CalendarEvent.builder()
                        .title("일일 이벤트 " + day + "-" + event)
                        .description("테스트 설명")
                        .startTime(baseDate.plusDays(day).plusHours(event))
                        .endTime(baseDate.plusDays(day).plusHours(event + 1))
                        .ownerUsername("testuser")
                        .category(testCategory)
                        .build();
                calendarEventRepository.save(calendarEvent);
            }
        }

        long startTime = System.currentTimeMillis();

        // 특정 월 조회 (12월 - 62개 이벤트 예상)
        mockMvc.perform(get("/api/calendar/events/month/2024/12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(62)); // 12월 31일 * 2개

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("월별 이벤트 조회 (730개 중 62개) 소요 시간: " + duration + "ms");

        // 성능 검증 (예: 500ms 이하)
        assertThat(duration).isLessThan(500);
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("겹치는 이벤트 검색 성능 테스트")
    void overlappingEventSearchPerformanceTest() throws Exception {
        // 겹치는 시간대의 이벤트들 생성
        LocalDateTime baseTime = LocalDateTime.now().plusDays(1);
        
        // 200개의 이벤트를 같은 날에 생성 (일부는 겹침)
        for (int i = 0; i < 200; i++) {
            CalendarEvent event = CalendarEvent.builder()
                    .title("겹침 테스트 이벤트 " + i)
                    .description("테스트 설명")
                    .startTime(baseTime.plusMinutes(i * 30)) // 30분씩 간격
                    .endTime(baseTime.plusMinutes(i * 30 + 60)) // 1시간 지속
                    .ownerUsername("testuser")
                    .category(testCategory)
                    .build();
            calendarEventRepository.save(event);
        }

        long startTime = System.currentTimeMillis();

        // 겹치는 이벤트 검색
        mockMvc.perform(get("/api/calendar/events/overlapping")
                .param("startTime", baseTime.plusHours(2).toString())
                .param("endTime", baseTime.plusHours(4).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("겹치는 이벤트 검색 (200개 중) 소요 시간: " + duration + "ms");

        // 성능 검증 (예: 300ms 이하)
        assertThat(duration).isLessThan(300);
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("카테고리별 이벤트 조회 성능 테스트")
    void categoryEventQueryPerformanceTest() throws Exception {
        // 여러 카테고리 생성
        List<EventCategory> categories = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            EventCategory category = EventCategory.builder()
                    .name("카테고리 " + i)
                    .color("#FF000" + i)
                    .description("테스트 카테고리 " + i)
                    .ownerUsername("testuser")
                    .build();
            categories.add(eventCategoryRepository.save(category));
        }

        // 각 카테고리에 50개씩 이벤트 생성 (총 500개)
        for (int catIndex = 0; catIndex < categories.size(); catIndex++) {
            EventCategory category = categories.get(catIndex);
            for (int eventIndex = 0; eventIndex < 50; eventIndex++) {
                CalendarEvent event = CalendarEvent.builder()
                        .title("카테고리 " + catIndex + " 이벤트 " + eventIndex)
                        .description("테스트 설명")
                        .startTime(LocalDateTime.now().plusDays(eventIndex))
                        .endTime(LocalDateTime.now().plusDays(eventIndex).plusHours(1))
                        .ownerUsername("testuser")
                        .category(category)
                        .build();
                calendarEventRepository.save(event);
            }
        }

        long startTime = System.currentTimeMillis();

        // 특정 카테고리의 이벤트 조회
        mockMvc.perform(get("/api/calendar/events/category/{categoryId}", categories.get(0).getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(50));

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("카테고리별 이벤트 조회 (500개 중 50개) 소요 시간: " + duration + "ms");

        // 성능 검증 (예: 200ms 이하)
        assertThat(duration).isLessThan(200);
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("동시 이벤트 수정 성능 테스트")
    void concurrentEventUpdatePerformanceTest() throws Exception {
        // 테스트 이벤트 생성
        CalendarEvent testEvent = CalendarEvent.builder()
                .title("동시 수정 테스트 이벤트")
                .description("원본 설명")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .ownerUsername("testuser")
                .category(testCategory)
                .build();
        CalendarEvent savedEvent = calendarEventRepository.save(testEvent);

        long startTime = System.currentTimeMillis();

        // 10번의 연속 수정 작업
        for (int i = 0; i < 10; i++) {
            CalendarEventRequest updateRequest = CalendarEventRequest.builder()
                    .title("수정된 제목 " + i)
                    .description("수정된 설명 " + i)
                    .startTime(LocalDateTime.now().plusDays(1).plusMinutes(i * 10))
                    .endTime(LocalDateTime.now().plusDays(1).plusMinutes(i * 10 + 60))
                    .categoryId(testCategory.getId())
                    .build();

            mockMvc.perform(put("/api/calendar/events/{eventId}", savedEvent.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("수정된 제목 " + i));
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("10번 연속 이벤트 수정 소요 시간: " + duration + "ms");
        System.out.println("수정당 평균 시간: " + (duration / 10) + "ms");

        // 성능 검증 (예: 수정당 50ms 이하)
        assertThat(duration / 10).isLessThan(50);
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("메모리 사용량 테스트 - 대량 데이터 처리")
    void memoryUsageTest() throws Exception {
        // 메모리 사용량 측정 시작
        Runtime runtime = Runtime.getRuntime();
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();

        // 1000개의 이벤트 생성 및 조회
        createBulkTestEvents(1000);

        // 전체 이벤트 조회
        mockMvc.perform(get("/api/calendar/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1000));

        // 메모리 사용량 측정 종료
        System.gc(); // 가비지 컬렉션 실행
        Thread.sleep(100); // GC 완료 대기
        
        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = memoryAfter - memoryBefore;

        System.out.println("1000개 이벤트 처리 메모리 사용량: " + (memoryUsed / 1024 / 1024) + "MB");

        // 메모리 사용량 검증 (예: 100MB 이하)
        assertThat(memoryUsed / 1024 / 1024).isLessThan(100);
    }

    // 헬퍼 메서드
    private void createBulkTestEvents(int count) {
        List<CalendarEvent> events = new ArrayList<>();
        LocalDateTime baseTime = LocalDateTime.now();

        for (int i = 0; i < count; i++) {
            CalendarEvent event = CalendarEvent.builder()
                    .title("대량 테스트 이벤트 " + i)
                    .description("대량 테스트용 설명 " + i)
                    .startTime(baseTime.plusDays(i % 100))
                    .endTime(baseTime.plusDays(i % 100).plusHours(1))
                    .ownerUsername("testuser")
                    .category(testCategory)
                    .build();
            events.add(event);

            // 배치 처리를 위해 100개씩 저장
            if (events.size() == 100) {
                calendarEventRepository.saveAll(events);
                events.clear();
            }
        }

        // 남은 이벤트 저장
        if (!events.isEmpty()) {
            calendarEventRepository.saveAll(events);
        }
    }
}