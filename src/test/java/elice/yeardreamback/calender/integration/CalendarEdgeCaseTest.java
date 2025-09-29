package elice.yeardreamback.calender.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import elice.yeardreamback.calender.dto.CalendarEventRequest;
import elice.yeardreamback.calender.dto.EventCategoryRequest;
import elice.yeardreamback.calender.entity.EventCategory;
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
import java.util.Arrays;
import java.util.Collections;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 캘린더 API 엣지 케이스 및 경계값 테스트
 * 극한 상황, 경계값, 예외 상황을 테스트합니다.
 */
@SpringBootTest
@AutoConfigureWebMvc
@Transactional
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName("캘린더 API 엣지 케이스 테스트")
class CalendarEdgeCaseTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

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

        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .build();
        userRepository.save(testUser);
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("최대 길이 문자열 테스트")
    void maxLengthStringTest() throws Exception {
        // 1. 최대 길이 제목 (100자)
        String maxTitle = "a".repeat(100);
        CalendarEventRequest maxTitleRequest = CalendarEventRequest.builder()
                .title(maxTitle)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .build();

        mockMvc.perform(post("/api/calendar/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(maxTitleRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(maxTitle));

        // 2. 제한 초과 제목 (101자)
        String overMaxTitle = "a".repeat(101);
        CalendarEventRequest overMaxTitleRequest = CalendarEventRequest.builder()
                .title(overMaxTitle)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .build();

        mockMvc.perform(post("/api/calendar/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(overMaxTitleRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.title").exists());

        // 3. 최대 길이 설명 (500자)
        String maxDescription = "b".repeat(500);
        CalendarEventRequest maxDescriptionRequest = CalendarEventRequest.builder()
                .title("테스트 제목")
                .description(maxDescription)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .build();

        mockMvc.perform(post("/api/calendar/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(maxDescriptionRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value(maxDescription));

        // 4. 최대 길이 장소 (200자)
        String maxLocation = "c".repeat(200);
        CalendarEventRequest maxLocationRequest = CalendarEventRequest.builder()
                .title("테스트 제목")
                .location(maxLocation)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .build();

        mockMvc.perform(post("/api/calendar/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(maxLocationRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.location").value(maxLocation));
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("특수 문자 및 유니코드 테스트")
    void specialCharacterAndUnicodeTest() throws Exception {
        // 1. 특수 문자가 포함된 이벤트
        CalendarEventRequest specialCharRequest = CalendarEventRequest.builder()
                .title("특수문자 테스트 !@#$%^&*()_+-=[]{}|;':\",./<>?")
                .description("설명에도 특수문자 ~`")
                .location("장소: 서울시 강남구 (1층)")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .build();

        mockMvc.perform(post("/api/calendar/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(specialCharRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("특수문자 테스트 !@#$%^&*()_+-=[]{}|;':\",./<>?"));

        // 2. 이모지가 포함된 이벤트
        CalendarEventRequest emojiRequest = CalendarEventRequest.builder()
                .title("이모지 테스트 🎉🎊🎈")
                .description("생일파티 🎂🎁")
                .location("카페 ☕")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .build();

        mockMvc.perform(post("/api/calendar/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emojiRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("이모지 테스트 🎉🎊🎈"));

        // 3. 다국어 텍스트
        CalendarEventRequest multiLanguageRequest = CalendarEventRequest.builder()
                .title("多言語テスト English 한국어 中文 العربية")
                .description("여러 언어가 섞인 설명입니다")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .build();

        mockMvc.perform(post("/api/calendar/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(multiLanguageRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("多言語テスト English 한국어 中文 العربية"));
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("극한 날짜/시간 테스트")
    void extremeDateTimeTest() throws Exception {
        // 1. 매우 먼 미래 날짜
        LocalDateTime farFuture = LocalDateTime.of(2099, 12, 31, 23, 59);
        CalendarEventRequest farFutureRequest = CalendarEventRequest.builder()
                .title("먼 미래 이벤트")
                .startTime(farFuture)
                .endTime(farFuture.plusMinutes(1))
                .build();

        mockMvc.perform(post("/api/calendar/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(farFutureRequest)))
                .andExpect(status().isCreated());

        // 2. 1분짜리 매우 짧은 이벤트
        LocalDateTime now = LocalDateTime.now().plusDays(1);
        CalendarEventRequest shortEventRequest = CalendarEventRequest.builder()
                .title("1분 이벤트")
                .startTime(now)
                .endTime(now.plusMinutes(1))
                .build();

        mockMvc.perform(post("/api/calendar/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(shortEventRequest)))
                .andExpect(status().isCreated());

        // 3. 매우 긴 이벤트 (30일)
        CalendarEventRequest longEventRequest = CalendarEventRequest.builder()
                .title("30일 이벤트")
                .startTime(now)
                .endTime(now.plusDays(30))
                .build();

        mockMvc.perform(post("/api/calendar/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(longEventRequest)))
                .andExpect(status().isCreated());

        // 4. 자정을 넘나드는 이벤트
        LocalDateTime beforeMidnight = LocalDateTime.now().plusDays(1).withHour(23).withMinute(30);
        CalendarEventRequest midnightCrossRequest = CalendarEventRequest.builder()
                .title("자정 넘나드는 이벤트")
                .startTime(beforeMidnight)
                .endTime(beforeMidnight.plusHours(2)) // 다음날 1:30
                .build();

        mockMvc.perform(post("/api/calendar/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(midnightCrossRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("알림 극한값 테스트")
    void extremeReminderTest() throws Exception {
        // 1. 최대 개수의 알림 (가정: 10개)
        CalendarEventRequest maxRemindersRequest = CalendarEventRequest.builder()
                .title("최대 알림 이벤트")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .reminderMinutes(Arrays.asList(1, 5, 10, 15, 30, 60, 120, 240, 480, 1440))
                .build();

        mockMvc.perform(post("/api/calendar/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(maxRemindersRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reminders.length()").value(10));

        // 2. 매우 큰 알림 시간 (1주일 전 = 10080분)
        CalendarEventRequest largeReminderRequest = CalendarEventRequest.builder()
                .title("큰 알림 시간 이벤트")
                .startTime(LocalDateTime.now().plusDays(8)) // 8일 후
                .endTime(LocalDateTime.now().plusDays(8).plusHours(1))
                .reminderMinutes(Arrays.asList(10080)) // 1주일 전
                .build();

        mockMvc.perform(post("/api/calendar/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(largeReminderRequest)))
                .andExpect(status().isCreated());

        // 3. 0분 알림 (즉시 알림)
        CalendarEventRequest immediateReminderRequest = CalendarEventRequest.builder()
                .title("즉시 알림 이벤트")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .reminderMinutes(Arrays.asList(0))
                .build();

        mockMvc.perform(post("/api/calendar/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(immediateReminderRequest)))
                .andExpect(status().isCreated());

        // 4. 음수 알림 시간 (유효성 검사 실패 예상)
        CalendarEventRequest negativeReminderRequest = CalendarEventRequest.builder()
                .title("음수 알림 이벤트")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .reminderMinutes(Arrays.asList(-30))
                .build();

        mockMvc.perform(post("/api/calendar/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(negativeReminderRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("카테고리 한계 테스트")
    void categoryLimitTest() throws Exception {
        // 1. 최대 개수의 카테고리 생성 (가정: 50개)
        for (int i = 0; i < 50; i++) {
            EventCategoryRequest categoryRequest = EventCategoryRequest.builder()
                    .name("카테고리 " + i)
                    .color(String.format("#%06X", i * 1000 % 0xFFFFFF))
                    .build();

            mockMvc.perform(post("/api/calendar/categories")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(categoryRequest)))
                    .andExpect(status().isCreated());
        }

        // 2. 51번째 카테고리 생성 시도 (실패 예상)
        EventCategoryRequest overLimitRequest = EventCategoryRequest.builder()
                .name("초과 카테고리")
                .color("#FF0000")
                .build();

        mockMvc.perform(post("/api/calendar/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(overLimitRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").containsString("최대"));

        // 3. 카테고리 개수 확인
        mockMvc.perform(get("/api/calendar/categories/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("50"));
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("빈 값 및 null 처리 테스트")
    void emptyAndNullValueTest() throws Exception {
        // 1. 빈 문자열 제목
        CalendarEventRequest emptyTitleRequest = CalendarEventRequest.builder()
                .title("")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .build();

        mockMvc.perform(post("/api/calendar/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyTitleRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.title").exists());

        // 2. 공백만 있는 제목
        CalendarEventRequest whitespaceTitleRequest = CalendarEventRequest.builder()
                .title("   ")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .build();

        mockMvc.perform(post("/api/calendar/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(whitespaceTitleRequest)))
                .andExpect(status().isBadRequest());

        // 3. null 설명 (허용되어야 함)
        CalendarEventRequest nullDescriptionRequest = CalendarEventRequest.builder()
                .title("유효한 제목")
                .description(null)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .build();

        mockMvc.perform(post("/api/calendar/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nullDescriptionRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").isEmpty());

        // 4. 빈 알림 목록 (허용되어야 함)
        CalendarEventRequest emptyRemindersRequest = CalendarEventRequest.builder()
                .title("알림 없는 이벤트")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .reminderMinutes(Collections.emptyList())
                .build();

        mockMvc.perform(post("/api/calendar/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyRemindersRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reminders").isArray())
                .andExpect(jsonPath("$.reminders.length()").value(0));
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("잘못된 색상 코드 테스트")
    void invalidColorCodeTest() throws Exception {
        // 1. 잘못된 HEX 형식들
        String[] invalidColors = {
                "FF0000",      // # 없음
                "#FF00",       // 너무 짧음
                "#FF00000",    // 너무 김
                "#GGGGGG",     // 잘못된 문자
                "#ff0000",     // 소문자 (허용될 수도 있음)
                "red",         // 색상 이름
                "rgb(255,0,0)", // RGB 형식
                "#12345G"      // 마지막 문자가 잘못됨
        };

        for (String invalidColor : invalidColors) {
            EventCategoryRequest invalidColorRequest = EventCategoryRequest.builder()
                    .name("잘못된 색상 테스트")
                    .color(invalidColor)
                    .build();

            mockMvc.perform(post("/api/calendar/categories")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidColorRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors.color").exists());
        }

        // 2. 유효한 색상 코드들
        String[] validColors = {
                "#FF0000", "#00FF00", "#0000FF",
                "#FFFFFF", "#000000", "#123456"
        };

        for (int i = 0; i < validColors.length; i++) {
            EventCategoryRequest validColorRequest = EventCategoryRequest.builder()
                    .name("유효한 색상 " + i)
                    .color(validColors[i])
                    .build();

            mockMvc.perform(post("/api/calendar/categories")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validColorRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.color").value(validColors[i]));
        }
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("동시성 및 경합 상태 테스트")
    void concurrencyAndRaceConditionTest() throws Exception {
        // 1. 같은 이름의 카테고리를 동시에 생성하려는 시도
        EventCategoryRequest categoryRequest = EventCategoryRequest.builder()
                .name("동시성 테스트 카테고리")
                .color("#FF0000")
                .build();

        // 첫 번째 요청은 성공해야 함
        mockMvc.perform(post("/api/calendar/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoryRequest)))
                .andExpect(status().isCreated());

        // 두 번째 요청은 실패해야 함 (중복 이름)
        mockMvc.perform(post("/api/calendar/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoryRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").containsString("이미 존재하는 카테고리 이름입니다"));
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("페이지네이션 경계값 테스트")
    void paginationBoundaryTest() throws Exception {
        // 테스트 데이터 생성 (100개 이벤트)
        EventCategory testCategory = EventCategory.builder()
                .name("페이지네이션 테스트")
                .color("#FF0000")
                .ownerUsername("testuser")
                .build();
        eventCategoryRepository.save(testCategory);

        for (int i = 0; i < 100; i++) {
            CalendarEventRequest eventRequest = CalendarEventRequest.builder()
                    .title("페이지네이션 테스트 이벤트 " + i)
                    .startTime(LocalDateTime.now().plusDays(i % 30))
                    .endTime(LocalDateTime.now().plusDays(i % 30).plusHours(1))
                    .categoryId(testCategory.getId())
                    .build();

            mockMvc.perform(post("/api/calendar/events")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(eventRequest)))
                    .andExpect(status().isCreated());
        }

        // 전체 이벤트 조회 (페이지네이션 없이)
        mockMvc.perform(get("/api/calendar/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(100));
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("잘못된 HTTP 메서드 테스트")
    void invalidHttpMethodTest() throws Exception {
        // 1. GET으로 이벤트 생성 시도
        mockMvc.perform(get("/api/calendar/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk()); // GET은 목록 조회이므로 성공

        // 2. POST로 이벤트 조회 시도 (잘못된 엔드포인트)
        mockMvc.perform(post("/api/calendar/events/1"))
                .andExpect(status().isMethodNotAllowed());

        // 3. DELETE로 이벤트 수정 시도
        mockMvc.perform(delete("/api/calendar/events/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isNotFound()); // 이벤트가 없으므로 404
    }
}