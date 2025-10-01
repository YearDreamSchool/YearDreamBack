package elice.yeardreamback.student.controller;

import elice.yeardreamback.student.dto.AttendanceMessage;
import elice.yeardreamback.student.dto.StudentResponse;
import elice.yeardreamback.student.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * 학생(Student) 및 출결(Attendance) 관련 요청을 처리하는 컨트롤러입니다.
 * REST API와 WebSocket(STOMP) 메시지 매핑을 모두 포함합니다.
 */
@RestController
@RequestMapping("api/students")
public class StudentController {

    private final StudentService studentService;

    /**
     * 의존성 주입을 위한 생성자입니다.
     * @param studentService 학생 관련 비즈니스 로직을 처리하는 서비스
     */
    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    /**
     * 전체 학생 목록을 조회합니다.
     * 학생의 기본 정보와 현재 출결 상태를 포함합니다.
     * @return 모든 학생의 정보를 담은 StudentResponse 리스트
     */
    @Operation(
            summary = "전체 학생 목록 조회 API",
            description = "현재 오프라인 강의실에 위치하고 있는 모든 학생들의 정보와 출결 정보를 위한 목록을 반환합니다. REST API 호출로 초기 데이터를 가져올 때 주로 사용됩니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "성공적으로 학생 목록을 조회하였습니다.",
            content = @Content(
                    mediaType = "application/json",
                    // List 형태임을 명확히 하기 위해 ArraySchema 사용
                    array = @ArraySchema(schema = @Schema(implementation = StudentResponse.class))
            )
    )
    @GetMapping
    public List<StudentResponse> findAll() {
        return studentService.findAll();
    }

    /**
     * WebSocket 메시지를 통해 특정 학생의 출결 상태를 업데이트하고,
     * 변경된 전체 목록을 구독 채널로 브로드캐스트합니다.
     * @param message 출결 업데이트에 필요한 정보(좌석 번호, 새로운 상태, 시간)를 담은 메시지
     * @return 상태가 업데이트된 후의 전체 학생 목록 (STOMP 채널로 전송됨)
     */
    @Operation(
            summary = "실시간 출결 상태 업데이트 (WebSocket)",
            description = "클라이언트(/app/attendance.update)로부터 STOMP 프로토콜 메시지를 받아 특정 학생의 출결 상태를 업데이트합니다. 처리 후, 변경된 **전체 학생 목록**이 `/topic/attendance` 채널로 브로드캐스트되어 모든 구독 클라이언트에게 실시간으로 전달됩니다."
    )
    @MessageMapping("/attendance.update")
    @SendTo("/topic/attendance")
    public List<StudentResponse> updateAttendance(AttendanceMessage message) {
        // 메시지에 담긴 업데이트 시간을 LocalDateTime으로 변환
        LocalDateTime updatedAt = LocalDateTime.ofInstant(
                Instant.parse(message.getUpdatedAt()),
                ZoneId.systemDefault() // 서버의 기본 시간대 사용
        );

        // 출결 상태 업데이트 비즈니스 로직 호출
        studentService.updateStatus(message.getSeatNum(), message.getNewStatus(), updatedAt);

        // 업데이트된 전체 목록을 다시 조회하여 STOMP 채널로 반환 (브로드캐스트)
        return studentService.findAll();
    }
}