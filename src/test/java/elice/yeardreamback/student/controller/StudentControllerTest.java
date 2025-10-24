package elice.yeardreamback.student.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import elice.yeardreamback.student.dto.AttendanceMessage;
import elice.yeardreamback.student.dto.StudentResponse;
import elice.yeardreamback.student.enums.StudentStatus;
import elice.yeardreamback.student.service.StudentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * StudentController의 REST API 및 WebSocket 핸들러 로직을 테스트합니다.
 * @WebMvcTest는 웹 계층(Controller)만 로드하여 빠르고 가볍게 테스트를 수행하며,
 * Spring Security 컨텍스트를 Mocking하여 인증 문제를 해결합니다.
 */
@WebMvcTest(StudentController.class)
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StudentController studentController; // @MessageMapping 호출을 위해 직접 주입

    // StudentService는 컨트롤러의 의존성이므로 MockBean으로 대체합니다.
    @MockitoBean
    private StudentService studentService;

    /**
     * 테스트에 사용할 Mock StudentResponse DTO를 생성합니다.
     */
    private StudentResponse createMockStudent(String name, int seatNum, StudentStatus status) {
        // StudentResponse DTO가 @Builder를 사용한다고 가정하고 Mock 객체를 생성합니다.
        // DTO 필드명이 seat라고 가정하고 seatNum 대신 사용합니다.
        return StudentResponse.builder()
                .name(name)
                .seat(seatNum) // DTO 필드명 'seat'으로 수정
                .status(status)
                .updatedAt(LocalDateTime.parse(LocalDateTime.now().toString()))
                .build();
    }


    // --- 1. REST API 테스트: GET /api/students ---

    @Test
    @DisplayName("GET /api/students - 전체 학생 목록 조회 성공 및 인증 적용")
    void findAll_Success() throws Exception {
        // Given
        List<StudentResponse> mockResponses = List.of(
                createMockStudent("김철수", 10, StudentStatus.PRESENT),
                createMockStudent("이영희", 11, StudentStatus.ABSENT)
        );
        when(studentService.findAll()).thenReturn(mockResponses);

        // When & Then
        mockMvc.perform(get("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        // Spring Security 인증을 Mocking하여 302 리다이렉션 방지
                        .with(user("test_user").roles("USER"))
                )
                .andExpect(status().isOk())
                // ✅ JSON Path 검증 복구 및 추가
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("김철수")))
                .andExpect(jsonPath("$[0].seat", is(10)))
                .andExpect(jsonPath("$[1].status", is(StudentStatus.ABSENT.name())));

        // Service 메서드 호출 검증
        verify(studentService, times(1)).findAll();
    }


    // --- 2. WebSocket Handler 테스트: updateAttendance ---

    @Test
    @DisplayName("WebSocket 핸들러 - 출결 상태 업데이트 서비스 호출 및 응답 검증")
    void updateAttendance_ServiceCallVerification() {
        // Given
        // 1. WebSocket 메시지 데이터 준비 (Setter 사용)
        StudentStatus newStatusEnum = StudentStatus.PRESENT;
        int seatNum = 5;
        Instant nowInstant = Instant.now();
        String nowString = nowInstant.toString(); // ISO 8601 형식

        AttendanceMessage message = new AttendanceMessage();
        message.setSeatNum(seatNum);
        message.setNewStatus(newStatusEnum);
        message.setUpdatedAt(nowString);

        // 2. 서비스가 반환할 Mock 데이터 (브로드캐스트용)
        List<StudentResponse> mockResponses = List.of(
                createMockStudent("박민수", seatNum, newStatusEnum)
        );

        // 3. studentService.findAll() Mocking
        when(studentService.findAll()).thenReturn(mockResponses);

        // When
        // @MessageMapping 핸들러는 메서드를 직접 호출하여 테스트
        List<StudentResponse> result = studentController.updateAttendance(message);

        // Then
        // 1. studentService.updateStatus가 올바른 인자와 함께 호출되었는지 검증
        LocalDateTime expectedUpdatedAt = LocalDateTime.ofInstant(nowInstant, ZoneId.systemDefault());

        // 🚨 ArgumentMatcher를 사용하여 인자 값 검증 (ENUM과 LocalDateTime 정확히 일치 확인)
        verify(studentService, times(1)).updateStatus(
                eq(seatNum),
                eq(newStatusEnum),
                eq(expectedUpdatedAt)
        );

        // 2. studentService.findAll이 호출되었는지 검증
        verify(studentService, times(1)).findAll();

        // 3. 반환 값 검증 (브로드캐스트 될 목록)
        assertEquals(mockResponses, result);
        assertEquals(newStatusEnum.name(), result.get(0).getStatus().name());
    }

    // --- 3. WebSocket Handler 테스트: updateAllAttendance ---
    @Test
    @DisplayName("WebSocket 핸들러 - 전체 출결 상태 업데이트 서비스 호출 및 응답 검증")
    void updateAllAttendance_ServiceCallVerification() {
        // Given: 전체 학생 상태 리스트
        StudentResponse s1 = createMockStudent("김철수", 1, StudentStatus.PRESENT);
        StudentResponse s2 = createMockStudent("이영희", 2, StudentStatus.LATE);
        StudentResponse s3 = createMockStudent("박민수", 3, null); // 상태가 null인 학생

        List<StudentResponse> inputStudents = List.of(s1, s2, s3);

        // Mock findAll 반환값
        when(studentService.findAll()).thenReturn(List.of(s1, s2, s3));

        // When: 컨트롤러 메서드 직접 호출
        List<StudentResponse> result = studentController.updateAllAttendance(inputStudents);

        // Then: 상태가 null이 아닌 학생만 updateStatus 호출
        verify(studentService, times(1)).updateStatus(eq(1), eq(StudentStatus.PRESENT), any(LocalDateTime.class));
        verify(studentService, times(1)).updateStatus(eq(2), eq(StudentStatus.LATE), any(LocalDateTime.class));
        // 상태가 null인 학생은 호출되지 않음
        verify(studentService, never()).updateStatus(eq(3), any(), any(LocalDateTime.class));

        // findAll이 호출되어 결과 반환
        verify(studentService, times(1)).findAll();

        // 반환 값 검증
        assertEquals(3, result.size());
        assertEquals(StudentStatus.PRESENT, result.get(0).getStatus());
        assertEquals(StudentStatus.LATE, result.get(1).getStatus());
        assertNull(result.get(2).getStatus());
    }

}
