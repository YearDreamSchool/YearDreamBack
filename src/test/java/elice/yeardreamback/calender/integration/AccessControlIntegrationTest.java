package elice.yeardreamback.calender.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import elice.yeardreamback.calender.dto.CalendarEventRequest;
import elice.yeardreamback.calender.dto.EventCategoryRequest;
import elice.yeardreamback.calender.entity.CalendarEvent;
import elice.yeardreamback.calender.entity.EventCategory;
import elice.yeardreamback.calender.entity.EventShare;
import elice.yeardreamback.calender.enums.EventStatus;
import elice.yeardreamback.calender.enums.SharePermission;
import elice.yeardreamback.calender.repository.CalendarEventRepository;
import elice.yeardreamback.calender.repository.EventCategoryRepository;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 접근 제어 통합 테스트
 * 실제 HTTP 요청을 통해 접근 제어가 올바르게 작동하는지 확인합니다.
 */
@SpringBootTest
@AutoConfigureWebMvc
@Transactional
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName("접근 제어 통합 테스트")
class AccessControlIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CalendarEventRepository calendarEventRepository;

    @Autowired
    private EventCategoryRepository eventCategoryRepository;

    @Autowired
    private EventShareRepository eventShareRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private User ownerUser;
    private User otherUser;
    private User sharedUser;
    private CalendarEvent testEvent;
    private EventCategory testCategory;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        // 테스트 사용자 생성
        ownerUser = User.builder()
                .username("owner")
                .email("owner@test.com")
                .build();
        userRepository.save(ownerUser);

        otherUser = User.builder()
                .username("other_user")
                .email("other@test.com")
                .build();
        userRepository.save(otherUser);

        sharedUser = User.builder()
                .username("shared_user")
                .email("shared@test.com")
                .build();
        userRepository.save(sharedUser);

        // 테스트 카테고리 생성
        testCategory = EventCategory.builder()
                .name("테스트 카테고리")
                .color("#FF0000")
                .description("테스트용 카테고리")
                .ownerUsername("owner")
                .build();
        eventCategoryRepository.save(testCategory);

        // 테스트 이벤트 생성
        testEvent = CalendarEvent.builder()
                .title("테스트 이벤트")
                .description("테스트용 이벤트")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .status(EventStatus.SCHEDULED)
                .ownerUsername("owner")
                .category(testCategory)
                .build();
        calendarEventRepository.save(testEvent);
    }

    @Test
    @WithMockUser(username = "owner")
    @DisplayName("이벤트 소유자 - 조회 성공")
    void getEvent_Owner_Success() throws Exception {
        mockMvc.perform(get("/api/calendar/events/{eventId}", testEvent.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testEvent.getId()))
                .andExpect(jsonPath("$.title").value("테스트 이벤트"))
                .andExpect(jsonPath("$.ownerUsername").value("owner"));
    }

    @Test
    @WithMockUser(username = "other_user")
    @DisplayName("이벤트 비소유자 - 조회 실패 (403 Forbidden)")
    void getEvent_NonOwner_Forbidden() throws Exception {
        mockMvc.perform(get("/api/calendar/events/{eventId}", testEvent.getId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Access Denied"))
                .andExpect(jsonPath("$.message").containsString("이벤트에 대한 접근 권한이 없습니다"));
    }

    @Test
    @WithMockUser(username = "shared_user")
    @DisplayName("공유받은 사용자 - 읽기 전용 권한으로 조회 성공")
    void getEvent_SharedUserViewOnly_Success() throws Exception {
        // 이벤트 공유 설정 (읽기 전용)
        EventShare eventShare = EventShare.builder()
                .eventId(testEvent.getId())
                .sharedByUsername("owner")
                .sharedWithUsername("shared_user")
                .permission(SharePermission.VIEW_ONLY)
                .build();
        eventShareRepository.save(eventShare);

        mockMvc.perform(get("/api/calendar/events/{eventId}", testEvent.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testEvent.getId()))
                .andExpect(jsonPath("$.title").value("테스트 이벤트"))
                .andExpect(jsonPath("$.isShared").value(true))
                .andExpect(jsonPath("$.canEdit").value(false));
    }

    @Test
    @WithMockUser(username = "shared_user")
    @DisplayName("공유받은 사용자 - 읽기 전용 권한으로 수정 실패")
    void updateEvent_SharedUserViewOnly_Forbidden() throws Exception {
        // 이벤트 공유 설정 (읽기 전용)
        EventShare eventShare = EventShare.builder()
                .eventId(testEvent.getId())
                .sharedByUsername("owner")
                .sharedWithUsername("shared_user")
                .permission(SharePermission.VIEW_ONLY)
                .build();
        eventShareRepository.save(eventShare);

        CalendarEventRequest updateRequest = CalendarEventRequest.builder()
                .title("수정된 제목")
                .description("수정된 설명")
                .startTime(LocalDateTime.now().plusDays(2))
                .endTime(LocalDateTime.now().plusDays(2).plusHours(1))
                .build();

        mockMvc.perform(put("/api/calendar/events/{eventId}", testEvent.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Access Denied"))
                .andExpect(jsonPath("$.message").containsString("이벤트에 대한 편집 권한이 없습니다"));
    }

    @Test
    @WithMockUser(username = "shared_user")
    @DisplayName("공유받은 사용자 - 편집 권한으로 수정 성공")
    void updateEvent_SharedUserEdit_Success() throws Exception {
        // 이벤트 공유 설정 (편집 권한)
        EventShare eventShare = EventShare.builder()
                .eventId(testEvent.getId())
                .sharedByUsername("owner")
                .sharedWithUsername("shared_user")
                .permission(SharePermission.EDIT)
                .build();
        eventShareRepository.save(eventShare);

        CalendarEventRequest updateRequest = CalendarEventRequest.builder()
                .title("수정된 제목")
                .description("수정된 설명")
                .startTime(LocalDateTime.now().plusDays(2))
                .endTime(LocalDateTime.now().plusDays(2).plusHours(1))
                .build();

        mockMvc.perform(put("/api/calendar/events/{eventId}", testEvent.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("수정된 제목"))
                .andExpect(jsonPath("$.description").value("수정된 설명"));
    }

    @Test
    @WithMockUser(username = "shared_user")
    @DisplayName("공유받은 사용자 - 편집 권한이 있어도 삭제 실패")
    void deleteEvent_SharedUserEdit_Forbidden() throws Exception {
        // 이벤트 공유 설정 (편집 권한)
        EventShare eventShare = EventShare.builder()
                .eventId(testEvent.getId())
                .sharedByUsername("owner")
                .sharedWithUsername("shared_user")
                .permission(SharePermission.EDIT)
                .build();
        eventShareRepository.save(eventShare);

        mockMvc.perform(delete("/api/calendar/events/{eventId}", testEvent.getId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Access Denied"))
                .andExpect(jsonPath("$.message").containsString("이벤트 삭제는 소유자만 가능합니다"));
    }

    @Test
    @WithMockUser(username = "owner")
    @DisplayName("이벤트 소유자 - 삭제 성공")
    void deleteEvent_Owner_Success() throws Exception {
        mockMvc.perform(delete("/api/calendar/events/{eventId}", testEvent.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "owner")
    @DisplayName("카테고리 소유자 - 조회 성공")
    void getCategory_Owner_Success() throws Exception {
        mockMvc.perform(get("/api/calendar/categories/{categoryId}", testCategory.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testCategory.getId()))
                .andExpect(jsonPath("$.name").value("테스트 카테고리"));
    }

    @Test
    @WithMockUser(username = "other_user")
    @DisplayName("카테고리 비소유자 - 조회 실패 (403 Forbidden)")
    void getCategory_NonOwner_Forbidden() throws Exception {
        mockMvc.perform(get("/api/calendar/categories/{categoryId}", testCategory.getId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Access Denied"))
                .andExpect(jsonPath("$.message").containsString("카테고리에 대한 접근 권한이 없습니다"));
    }

    @Test
    @WithMockUser(username = "other_user")
    @DisplayName("카테고리 비소유자 - 수정 실패 (403 Forbidden)")
    void updateCategory_NonOwner_Forbidden() throws Exception {
        EventCategoryRequest updateRequest = EventCategoryRequest.builder()
                .name("수정된 카테고리")
                .color("#00FF00")
                .description("수정된 설명")
                .build();

        mockMvc.perform(put("/api/calendar/categories/{categoryId}", testCategory.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Access Denied"))
                .andExpect(jsonPath("$.message").containsString("카테고리에 대한 접근 권한이 없습니다"));
    }

    @Test
    @WithMockUser(username = "other_user")
    @DisplayName("카테고리 비소유자 - 삭제 실패 (403 Forbidden)")
    void deleteCategory_NonOwner_Forbidden() throws Exception {
        mockMvc.perform(delete("/api/calendar/categories/{categoryId}", testCategory.getId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Access Denied"))
                .andExpect(jsonPath("$.message").containsString("카테고리에 대한 접근 권한이 없습니다"));
    }

    @Test
    @WithMockUser(username = "owner")
    @DisplayName("카테고리 소유자 - 수정 성공")
    void updateCategory_Owner_Success() throws Exception {
        EventCategoryRequest updateRequest = EventCategoryRequest.builder()
                .name("수정된 카테고리")
                .color("#00FF00")
                .description("수정된 설명")
                .build();

        mockMvc.perform(put("/api/calendar/categories/{categoryId}", testCategory.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("수정된 카테고리"))
                .andExpect(jsonPath("$.color").value("#00FF00"))
                .andExpect(jsonPath("$.description").value("수정된 설명"));
    }

    @Test
    @WithMockUser(username = "anonymous")
    @DisplayName("존재하지 않는 이벤트 - 404 Not Found")
    void getEvent_NotFound() throws Exception {
        mockMvc.perform(get("/api/calendar/events/{eventId}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Event Not Found"))
                .andExpect(jsonPath("$.message").containsString("이벤트를 찾을 수 없습니다"));
    }

    @Test
    @WithMockUser(username = "anonymous")
    @DisplayName("존재하지 않는 카테고리 - 404 Not Found")
    void getCategory_NotFound() throws Exception {
        mockMvc.perform(get("/api/calendar/categories/{categoryId}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Category Not Found"))
                .andExpect(jsonPath("$.message").containsString("카테고리를 찾을 수 없습니다"));
    }
}