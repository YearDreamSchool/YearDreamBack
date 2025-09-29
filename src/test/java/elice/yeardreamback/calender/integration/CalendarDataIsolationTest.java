package elice.yeardreamback.calender.integration;

import elice.yeardreamback.calender.dto.CalendarEventResponse;
import elice.yeardreamback.calender.dto.EventCategoryResponse;
import elice.yeardreamback.calender.service.CalendarEventService;
import elice.yeardreamback.calender.service.EventCategoryService;
import elice.yeardreamback.jwt.JWTUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 캘린더 API 사용자별 데이터 격리 테스트
 */
@SpringBootTest
@AutoConfigureWebMvc
class CalendarDataIsolationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JWTUtil jwtUtil;

    @MockBean
    private CalendarEventService calendarEventService;

    @MockBean
    private EventCategoryService eventCategoryService;

    private String user1Token;
    private String user2Token;
    private CalendarEventResponse user1Event;
    private CalendarEventResponse user2Event;
    private EventCategoryResponse user1Category;
    private EventCategoryResponse user2Category;

    @BeforeEach
    void setUp() {
        // 두 명의 다른 사용자를 위한 JWT 토큰 생성
        user1Token = jwtUtil.createJwt("user1", "USER", 60 * 60 * 1000L);
        user2Token = jwtUtil.createJwt("user2", "USER", 60 * 60 * 1000L);

        // 사용자별 테스트 데이터 설정
        user1Event = new CalendarEventResponse();
        user1Event.setId(1L);
        user1Event.setTitle("User1의 이벤트");
        user1Event.setOwnerUsername("user1");

        user2Event = new CalendarEventResponse();
        user2Event.setId(2L);
        user2Event.setTitle("User2의 이벤트");
        user2Event.setOwnerUsername("user2");

        user1Category = new EventCategoryResponse();
        user1Category.setId(1L);
        user1Category.setName("User1의 카테고리");

        user2Category = new EventCategoryResponse();
        user2Category.setId(2L);
        user2Category.setName("User2의 카테고리");
    }

    @Test
    @DisplayName("사용자1은 자신의 이벤트만 조회할 수 있음")
    void user1CanOnlyAccessOwnEvents() throws Exception {
        // given
        when(calendarEventService.getUserEvents("user1")).thenReturn(Arrays.asList(user1Event));
        when(calendarEventService.getUserEvents("user2")).thenReturn(Arrays.asList(user2Event));

        // when & then - user1으로 요청
        mockMvc.perform(get("/api/calendar/events")
                .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].title").value("User1의 이벤트"))
                .andExpect(jsonPath("$[0].ownerUsername").value("user1"));
    }

    @Test
    @DisplayName("사용자2는 자신의 이벤트만 조회할 수 있음")
    void user2CanOnlyAccessOwnEvents() throws Exception {
        // given
        when(calendarEventService.getUserEvents("user2")).thenReturn(Arrays.asList(user2Event));

        // when & then - user2로 요청
        mockMvc.perform(get("/api/calendar/events")
                .header("Authorization", "Bearer " + user2Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(2L))
                .andExpect(jsonPath("$[0].title").value("User2의 이벤트"))
                .andExpect(jsonPath("$[0].ownerUsername").value("user2"));
    }

    @Test
    @DisplayName("사용자1은 자신의 카테고리만 조회할 수 있음")
    void user1CanOnlyAccessOwnCategories() throws Exception {
        // given
        when(eventCategoryService.getUserCategories("user1")).thenReturn(Arrays.asList(user1Category));

        // when & then - user1으로 요청
        mockMvc.perform(get("/api/calendar/categories")
                .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("User1의 카테고리"));
    }

    @Test
    @DisplayName("사용자2는 자신의 카테고리만 조회할 수 있음")
    void user2CanOnlyAccessOwnCategories() throws Exception {
        // given
        when(eventCategoryService.getUserCategories("user2")).thenReturn(Arrays.asList(user2Category));

        // when & then - user2로 요청
        mockMvc.perform(get("/api/calendar/categories")
                .header("Authorization", "Bearer " + user2Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(2L))
                .andExpect(jsonPath("$[0].name").value("User2의 카테고리"));
    }

    @Test
    @DisplayName("사용자별로 다른 공유 이벤트 목록 조회")
    void usersHaveDifferentSharedEvents() throws Exception {
        // given
        CalendarEventResponse sharedWithUser1 = new CalendarEventResponse();
        sharedWithUser1.setId(3L);
        sharedWithUser1.setTitle("User1과 공유된 이벤트");
        sharedWithUser1.setOwnerUsername("otheruser");

        CalendarEventResponse sharedWithUser2 = new CalendarEventResponse();
        sharedWithUser2.setId(4L);
        sharedWithUser2.setTitle("User2와 공유된 이벤트");
        sharedWithUser2.setOwnerUsername("anotheruser");

        when(calendarEventService.getSharedEvents("user1")).thenReturn(Arrays.asList(sharedWithUser1));
        when(calendarEventService.getSharedEvents("user2")).thenReturn(Arrays.asList(sharedWithUser2));

        // when & then - user1으로 공유 이벤트 조회
        mockMvc.perform(get("/api/calendar/events/shared")
                .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(3L))
                .andExpect(jsonPath("$[0].title").value("User1과 공유된 이벤트"));

        // when & then - user2로 공유 이벤트 조회
        mockMvc.perform(get("/api/calendar/events/shared")
                .header("Authorization", "Bearer " + user2Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(4L))
                .andExpect(jsonPath("$[0].title").value("User2와 공유된 이벤트"));
    }

    @Test
    @DisplayName("사용자가 데이터가 없는 경우 빈 배열 반환")
    void userWithNoDataReturnsEmptyArray() throws Exception {
        // given
        when(calendarEventService.getUserEvents("user1")).thenReturn(Collections.emptyList());
        when(eventCategoryService.getUserCategories("user1")).thenReturn(Collections.emptyList());

        // when & then - 이벤트 조회
        mockMvc.perform(get("/api/calendar/events")
                .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        // when & then - 카테고리 조회
        mockMvc.perform(get("/api/calendar/categories")
                .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("동일한 엔드포인트에서 사용자별로 다른 응답")
    void sameEndpointDifferentResponsePerUser() throws Exception {
        // given
        when(calendarEventService.getUserEvents("user1")).thenReturn(Arrays.asList(user1Event));
        when(calendarEventService.getUserEvents("user2")).thenReturn(Arrays.asList(user2Event));

        // when & then - 동일한 엔드포인트, 다른 토큰으로 요청
        mockMvc.perform(get("/api/calendar/events")
                .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ownerUsername").value("user1"));

        mockMvc.perform(get("/api/calendar/events")
                .header("Authorization", "Bearer " + user2Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ownerUsername").value("user2"));
    }
}