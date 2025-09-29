package elice.yeardreamback.calender.controller;

import elice.yeardreamback.calender.dto.EventCategoryRequest;
import elice.yeardreamback.calender.dto.EventCategoryResponse;
import elice.yeardreamback.calender.service.EventCategoryService;
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
 * EventCategoryController 테스트
 */
@WebMvcTest(EventCategoryController.class)
class EventCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventCategoryService eventCategoryService;

    @Autowired
    private ObjectMapper objectMapper;

    private EventCategoryRequest testRequest;
    private EventCategoryResponse testResponse;

    @BeforeEach
    void setUp() {
        testRequest = new EventCategoryRequest("업무", "#FF0000", "업무 관련 일정");

        testResponse = new EventCategoryResponse();
        testResponse.setId(1L);
        testResponse.setName("업무");
        testResponse.setColor("#FF0000");
        testResponse.setDescription("업무 관련 일정");
        testResponse.setEventCount(0);
        testResponse.setCanBeDeleted(true);
        testResponse.setCreatedAt(LocalDateTime.now());
        testResponse.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("카테고리 생성 성공 테스트")
    @WithMockUser(username = "testuser")
    void createCategorySuccess() throws Exception {
        // given
        when(eventCategoryService.createCategory(eq("testuser"), any(EventCategoryRequest.class)))
            .thenReturn(testResponse);

        // when & then
        mockMvc.perform(post("/api/calendar/categories")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("업무"))
                .andExpect(jsonPath("$.color").value("#FF0000"))
                .andExpect(jsonPath("$.description").value("업무 관련 일정"));

        verify(eventCategoryService).createCategory(eq("testuser"), any(EventCategoryRequest.class));
    }

    @Test
    @DisplayName("유효하지 않은 데이터로 카테고리 생성 시 400 에러")
    @WithMockUser(username = "testuser")
    void createCategoryWithInvalidData() throws Exception {
        // given
        EventCategoryRequest invalidRequest = new EventCategoryRequest();
        invalidRequest.setName(""); // 빈 이름
        invalidRequest.setColor("invalid-color"); // 잘못된 색상 형식

        // when & then
        mockMvc.perform(post("/api/calendar/categories")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(eventCategoryService, never()).createCategory(anyString(), any(EventCategoryRequest.class));
    }

    @Test
    @DisplayName("카테고리 수정 성공 테스트")
    @WithMockUser(username = "testuser")
    void updateCategorySuccess() throws Exception {
        // given
        EventCategoryRequest updateRequest = new EventCategoryRequest("수정된 업무", "#00FF00", "수정된 설명");
        EventCategoryResponse updatedResponse = new EventCategoryResponse();
        updatedResponse.setId(1L);
        updatedResponse.setName("수정된 업무");
        updatedResponse.setColor("#00FF00");
        updatedResponse.setDescription("수정된 설명");

        when(eventCategoryService.updateCategory(eq("testuser"), eq(1L), any(EventCategoryRequest.class)))
            .thenReturn(updatedResponse);

        // when & then
        mockMvc.perform(put("/api/calendar/categories/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("수정된 업무"))
                .andExpect(jsonPath("$.color").value("#00FF00"));

        verify(eventCategoryService).updateCategory(eq("testuser"), eq(1L), any(EventCategoryRequest.class));
    }

    @Test
    @DisplayName("카테고리 삭제 성공 테스트")
    @WithMockUser(username = "testuser")
    void deleteCategorySuccess() throws Exception {
        // given
        doNothing().when(eventCategoryService).deleteCategory("testuser", 1L);

        // when & then
        mockMvc.perform(delete("/api/calendar/categories/1")
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(eventCategoryService).deleteCategory("testuser", 1L);
    }

    @Test
    @DisplayName("카테고리 조회 성공 테스트")
    @WithMockUser(username = "testuser")
    void getCategorySuccess() throws Exception {
        // given
        when(eventCategoryService.getCategory("testuser", 1L)).thenReturn(testResponse);

        // when & then
        mockMvc.perform(get("/api/calendar/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("업무"))
                .andExpect(jsonPath("$.color").value("#FF0000"));

        verify(eventCategoryService).getCategory("testuser", 1L);
    }

    @Test
    @DisplayName("사용자 카테고리 목록 조회 테스트")
    @WithMockUser(username = "testuser")
    void getUserCategoriesSuccess() throws Exception {
        // given
        List<EventCategoryResponse> responses = Arrays.asList(testResponse);
        when(eventCategoryService.getUserCategories("testuser")).thenReturn(responses);

        // when & then
        mockMvc.perform(get("/api/calendar/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("업무"));

        verify(eventCategoryService).getUserCategories("testuser");
    }

    @Test
    @DisplayName("이벤트가 있는 카테고리 조회 테스트")
    @WithMockUser(username = "testuser")
    void getCategoriesWithEventsSuccess() throws Exception {
        // given
        testResponse.setEventCount(5);
        testResponse.setCanBeDeleted(false);
        List<EventCategoryResponse> responses = Arrays.asList(testResponse);
        when(eventCategoryService.getCategoriesWithEvents("testuser")).thenReturn(responses);

        // when & then
        mockMvc.perform(get("/api/calendar/categories/with-events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].eventCount").value(5))
                .andExpect(jsonPath("$[0].canBeDeleted").value(false));

        verify(eventCategoryService).getCategoriesWithEvents("testuser");
    }

    @Test
    @DisplayName("이벤트가 없는 카테고리 조회 테스트")
    @WithMockUser(username = "testuser")
    void getCategoriesWithoutEventsSuccess() throws Exception {
        // given
        List<EventCategoryResponse> responses = Arrays.asList(testResponse);
        when(eventCategoryService.getCategoriesWithoutEvents("testuser")).thenReturn(responses);

        // when & then
        mockMvc.perform(get("/api/calendar/categories/without-events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].eventCount").value(0))
                .andExpect(jsonPath("$[0].canBeDeleted").value(true));

        verify(eventCategoryService).getCategoriesWithoutEvents("testuser");
    }

    @Test
    @DisplayName("카테고리 이름 중복 확인 테스트")
    @WithMockUser(username = "testuser")
    void checkDuplicateNameSuccess() throws Exception {
        // given
        when(eventCategoryService.isDuplicateName("testuser", "업무", null)).thenReturn(true);
        when(eventCategoryService.isDuplicateName("testuser", "개인", null)).thenReturn(false);

        // when & then - 중복된 이름
        mockMvc.perform(get("/api/calendar/categories/check-duplicate")
                .param("name", "업무"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        // when & then - 중복되지 않은 이름
        mockMvc.perform(get("/api/calendar/categories/check-duplicate")
                .param("name", "개인"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(eventCategoryService).isDuplicateName("testuser", "업무", null);
        verify(eventCategoryService).isDuplicateName("testuser", "개인", null);
    }

    @Test
    @DisplayName("카테고리 이름 중복 확인 (제외 ID 포함) 테스트")
    @WithMockUser(username = "testuser")
    void checkDuplicateNameWithExcludeIdSuccess() throws Exception {
        // given
        when(eventCategoryService.isDuplicateName("testuser", "업무", 1L)).thenReturn(false);

        // when & then
        mockMvc.perform(get("/api/calendar/categories/check-duplicate")
                .param("name", "업무")
                .param("excludeCategoryId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(eventCategoryService).isDuplicateName("testuser", "업무", 1L);
    }

    @Test
    @DisplayName("색상별 카테고리 개수 조회 테스트")
    @WithMockUser(username = "testuser")
    void countCategoriesByColorSuccess() throws Exception {
        // given
        when(eventCategoryService.countCategoriesByColor("testuser", "#FF0000")).thenReturn(3L);

        // when & then
        mockMvc.perform(get("/api/calendar/categories/count-by-color")
                .param("color", "#FF0000"))
                .andExpect(status().isOk())
                .andExpect(content().string("3"));

        verify(eventCategoryService).countCategoriesByColor("testuser", "#FF0000");
    }

    @Test
    @DisplayName("사용자 카테고리 총 개수 조회 테스트")
    @WithMockUser(username = "testuser")
    void getUserCategoryCountSuccess() throws Exception {
        // given
        when(eventCategoryService.getUserCategoryCount("testuser")).thenReturn(10L);

        // when & then
        mockMvc.perform(get("/api/calendar/categories/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("10"));

        verify(eventCategoryService).getUserCategoryCount("testuser");
    }

    @Test
    @DisplayName("인증되지 않은 사용자 접근 시 401 에러")
    void accessWithoutAuthentication() throws Exception {
        // when & then
        mockMvc.perform(get("/api/calendar/categories"))
                .andExpect(status().isUnauthorized());

        verify(eventCategoryService, never()).getUserCategories(anyString());
    }

    @Test
    @DisplayName("유효하지 않은 색상 형식으로 카테고리 생성 시 400 에러")
    @WithMockUser(username = "testuser")
    void createCategoryWithInvalidColorFormat() throws Exception {
        // given
        EventCategoryRequest invalidRequest = new EventCategoryRequest("테스트", "red", "설명");

        // when & then
        mockMvc.perform(post("/api/calendar/categories")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(eventCategoryService, never()).createCategory(anyString(), any(EventCategoryRequest.class));
    }

    @Test
    @DisplayName("카테고리 이름이 50자를 초과하는 경우 400 에러")
    @WithMockUser(username = "testuser")
    void createCategoryWithTooLongName() throws Exception {
        // given
        String longName = "a".repeat(51); // 51자
        EventCategoryRequest invalidRequest = new EventCategoryRequest(longName, "#FF0000", "설명");

        // when & then
        mockMvc.perform(post("/api/calendar/categories")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(eventCategoryService, never()).createCategory(anyString(), any(EventCategoryRequest.class));
    }
}