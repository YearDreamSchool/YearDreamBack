package elice.yeardreamback.student.controller;

import elice.yeardreamback.oauth.dto.AttendanceMessage;
import elice.yeardreamback.student.dto.StudentResponse;
import elice.yeardreamback.student.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
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

@RestController
@RequestMapping("api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    // ------------------------------------------------------------------
    // HTTP GET /api/students: 전체 학생 목록 조회
    // ------------------------------------------------------------------
    @Operation(
            summary = "전체 학생 목록 조회 API",
            description = "현재 오프라인 강의실에 위치하고 있는 모든 학생들의 정보와 출결 정보를 위한 목록 반환 API입니다.")
    @ApiResponse(
            responseCode = "200",
            description = "성공적으로 학생 목록을 조회하였습니다.",
            content = {@Content(mediaType = "application/json",
                    schema = @Schema(implementation = StudentResponse.class))})
    @GetMapping
    public List<StudentResponse> findAll() {
        return studentService.findAll();
    }

    // ------------------------------------------------------------------
    // WebSocket STOMP /app/attendance.update: 실시간 출결 상태 업데이트
    // ------------------------------------------------------------------
    @Operation(
            summary = "실시간 출결 상태 업데이트 (WebSocket)",
            description = "STOMP 프로토콜을 사용하여 특정 학생의 출결 상태를 업데이트합니다. 이 메시지를 처리한 후, 변경된 전체 학생 목록이 '/topic/attendance' 채널로 브로드캐스트됩니다."
    )
    @MessageMapping("/attendance.update")
    @SendTo("/topic/attendance")
    public List<StudentResponse> updateAttendance(AttendanceMessage message) {
        // UTC 문자열 -> LocalDateTime 변환
        LocalDateTime updatedAt = LocalDateTime.ofInstant(
                Instant.parse(message.getUpdatedAt()),
                ZoneId.systemDefault()
        );

        studentService.updateStatus(message.getSeatNum(), message.getNewStatus(), updatedAt);
        return studentService.findAll();
    }
}
