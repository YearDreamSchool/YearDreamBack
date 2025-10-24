package elice.yeardreamback.student.service;

import elice.yeardreamback.student.dto.StudentResponse;
import elice.yeardreamback.student.entity.Students;
import elice.yeardreamback.student.enums.StudentStatus;
import elice.yeardreamback.student.mapper.StudentMapper;
import elice.yeardreamback.student.repository.StudentRepository;
import elice.yeardreamback.student.service.impl.StudentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceImplTest {

    @InjectMocks
    private StudentServiceImpl studentServiceImpl;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private StudentMapper studentMapper;

    private Students student1;
    private Students student2;
    private StudentResponse studentResponse1;
    private StudentResponse studentResponse2;
    private final LocalDateTime NOW = LocalDateTime.of(2025, 10, 2, 10, 0);

    private Students createMockEntity(Long id, String name, int seatNum, StudentStatus status, LocalDateTime updatedAt) {
        Students student = new Students();
        student.setId(id);
        student.setName(name);
        student.setSeatNum(seatNum);
        student.setStatus(status);
        student.setUpdatedAt(updatedAt);
        return student;
    }

    private StudentResponse createMockResponse(Long id, String name, int seatNum, StudentStatus status, LocalDateTime updatedAt) {
        return new StudentResponse(id, name, seatNum, status, updatedAt);
    }

    @BeforeEach
    void setUp() {
        student1 = createMockEntity(1L, "김철수", 10, StudentStatus.PRESENT, NOW.minusDays(1));
        student2 = createMockEntity(2L, "이영희", 11, StudentStatus.ABSENT, NOW.minusHours(1));

        studentResponse1 = createMockResponse(1L, "김철수", 10, StudentStatus.PRESENT, NOW.minusDays(1));
        studentResponse2 = createMockResponse(2L, "이영희", 11, StudentStatus.ABSENT, NOW.minusHours(1));
    }

    // -------------------------------------------------------------------
    // 1. findAll() 테스트
    // -------------------------------------------------------------------
    @Test
    @DisplayName("전체 학생 목록 조회 성공 - 2명의 학생 반환")
    void findAll_Success_ReturnsList() {
        // Given
        List<Students> mockEntities = List.of(student1, student2);
        when(studentRepository.findAll()).thenReturn(mockEntities);

        // When
        List<StudentResponse> result = studentServiceImpl.findAll();

        // Then
        // 1. Repository 호출 검증
        verify(studentRepository, times(1)).findAll();

        // 2. 결과 목록 검증 (크기와 내용)
        assertNotNull(result);
        assertEquals(2, result.size());

        // Service 로직에서 DTO 생성자가 호출되었으므로, 필드를 직접 검증합니다.
        assertEquals(student1.getName(), result.get(0).getName());
        assertEquals(student2.getSeatNum(), result.get(1).getSeat());
    }

    @Test
    @DisplayName("전체 학생 목록 조회 성공 - 빈 목록 반환")
    void findAll_Success_ReturnsEmptyList() {
        // Given
        when(studentRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<StudentResponse> result = studentServiceImpl.findAll();

        // Then
        verify(studentRepository, times(1)).findAll();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // -------------------------------------------------------------------
    // 2. updateStatus() 테스트
    // -------------------------------------------------------------------

    @Test
    @DisplayName("출결 상태 업데이트 성공")
    void updateStatus_Success() {
        // Given
        int seatNum = student1.getSeatNum();
        StudentStatus newStatus = StudentStatus.LATE;
        LocalDateTime updateTime = NOW.plusMinutes(10);

        // 1. findBySeatNum Mocking: student1을 반환하도록 설정
        when(studentRepository.findBySeatNum(eq(seatNum))).thenReturn(student1);

        // 2. studentMapper Mocking: 업데이트된 student1을 DTO로 변환하도록 설정
        StudentResponse updatedResponse = createMockResponse(
                student1.getId(), student1.getName(), seatNum, newStatus, updateTime
        );
        when(studentMapper.toResponseDto(any(Students.class))).thenReturn(updatedResponse);

        // When
        StudentResponse result = studentServiceImpl.updateStatus(seatNum, newStatus, updateTime);

        // Then
        // 1. Repository 호출 검증
        verify(studentRepository, times(1)).findBySeatNum(eq(seatNum));

        // 2. 엔티티 상태 변경 검증 (setter 호출 후 save 전에 변경되었는지 확인)
        assertEquals(newStatus, student1.getStatus());
        assertEquals(updateTime, student1.getUpdatedAt());

        // 3. save 호출 검증 (변경된 엔티티가 저장되었는지 확인)
        verify(studentRepository, times(1)).save(eq(student1));

        // 4. Mapper 호출 검증
        verify(studentMapper, times(1)).toResponseDto(eq(student1));

        // 5. 결과 값 검증
        assertEquals(updatedResponse, result);
    }

    @Test
    @DisplayName("출결 상태 업데이트 실패 - 학생 미존재 시 IllegalArgumentException 발생")
    void updateStatus_Fail_StudentNotFound() {
        // Given
        int nonExistentSeatNum = 99;
        StudentStatus newStatus = StudentStatus.LATE;
        LocalDateTime updateTime = NOW.plusMinutes(10);

        // findBySeatNum이 null을 반환하도록 설정 (학생 미존재)
        when(studentRepository.findBySeatNum(eq(nonExistentSeatNum))).thenReturn(null);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            studentServiceImpl.updateStatus(nonExistentSeatNum, newStatus, updateTime);
        });

        // 1. save와 Mapper는 호출되지 않아야 합니다.
        verify(studentRepository, never()).save(any(Students.class));
        verify(studentMapper, never()).toResponseDto(any(Students.class));
    }
}