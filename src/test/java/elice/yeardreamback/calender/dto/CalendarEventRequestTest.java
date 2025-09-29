package elice.yeardreamback.calender.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * CalendarEventRequest DTO 테스트
 */
class CalendarEventRequestTest {

    private Validator validator;
    private LocalDateTime validStartTime;
    private LocalDateTime validEndTime;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        
        validStartTime = LocalDateTime.now().plusHours(1);
        validEndTime = validStartTime.plusHours(2);
    }

    @Test
    @DisplayName("유효한 요청 DTO 생성 테스트")
    void createValidRequest() {
        // given
        CalendarEventRequest request = new CalendarEventRequest(
            "테스트 이벤트",
            "테스트 설명",
            validStartTime,
            validEndTime,
            "테스트 장소",
            1L,
            Arrays.asList(30, 60)
        );

        // when
        Set<ConstraintViolation<CalendarEventRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isEmpty();
        assertThat(request.isValidTimeRange()).isTrue();
    }

    @Test
    @DisplayName("제목이 null인 경우 유효성 검사 실패")
    void failValidationWhenTitleIsNull() {
        // given
        CalendarEventRequest request = new CalendarEventRequest(
            null,
            "테스트 설명",
            validStartTime,
            validEndTime,
            "테스트 장소",
            1L,
            Arrays.asList(30)
        );

        // when
        Set<ConstraintViolation<CalendarEventRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("이벤트 제목은 필수입니다");
    }

    @Test
    @DisplayName("제목이 빈 문자열인 경우 유효성 검사 실패")
    void failValidationWhenTitleIsEmpty() {
        // given
        CalendarEventRequest request = new CalendarEventRequest(
            "   ",
            "테스트 설명",
            validStartTime,
            validEndTime,
            "테스트 장소",
            1L,
            Arrays.asList(30)
        );

        // when
        Set<ConstraintViolation<CalendarEventRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("이벤트 제목은 필수입니다");
    }

    @Test
    @DisplayName("제목이 100자를 초과하는 경우 유효성 검사 실패")
    void failValidationWhenTitleTooLong() {
        // given
        String longTitle = "a".repeat(101);
        CalendarEventRequest request = new CalendarEventRequest(
            longTitle,
            "테스트 설명",
            validStartTime,
            validEndTime,
            "테스트 장소",
            1L,
            Arrays.asList(30)
        );

        // when
        Set<ConstraintViolation<CalendarEventRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("이벤트 제목은 100자를 초과할 수 없습니다");
    }

    @Test
    @DisplayName("시작 시간이 null인 경우 유효성 검사 실패")
    void failValidationWhenStartTimeIsNull() {
        // given
        CalendarEventRequest request = new CalendarEventRequest(
            "테스트 이벤트",
            "테스트 설명",
            null,
            validEndTime,
            "테스트 장소",
            1L,
            Arrays.asList(30)
        );

        // when
        Set<ConstraintViolation<CalendarEventRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("이벤트 시작 시간은 필수입니다");
    }

    @Test
    @DisplayName("종료 시간이 null인 경우 유효성 검사 실패")
    void failValidationWhenEndTimeIsNull() {
        // given
        CalendarEventRequest request = new CalendarEventRequest(
            "테스트 이벤트",
            "테스트 설명",
            validStartTime,
            null,
            "테스트 장소",
            1L,
            Arrays.asList(30)
        );

        // when
        Set<ConstraintViolation<CalendarEventRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("이벤트 종료 시간은 필수입니다");
    }

    @Test
    @DisplayName("시작 시간이 종료 시간보다 늦은 경우 시간 범위 유효성 검사 실패")
    void failTimeRangeValidationWhenStartTimeAfterEndTime() {
        // given
        CalendarEventRequest request = new CalendarEventRequest(
            "테스트 이벤트",
            "테스트 설명",
            validEndTime,
            validStartTime, // 시작과 종료 시간 바뀜
            "테스트 장소",
            1L,
            Arrays.asList(30)
        );

        // when & then
        assertThat(request.isValidTimeRange()).isFalse();
    }

    @Test
    @DisplayName("알림 시간에 음수가 포함된 경우 유효성 검사 실패")
    void failValidationWhenReminderMinutesContainNegative() {
        // given
        CalendarEventRequest request = new CalendarEventRequest(
            "테스트 이벤트",
            "테스트 설명",
            validStartTime,
            validEndTime,
            "테스트 장소",
            1L,
            Arrays.asList(30, -10) // 음수 포함
        );

        // when
        Set<ConstraintViolation<CalendarEventRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("알림 시간은 0분 이상이어야 합니다");
    }

    @Test
    @DisplayName("선택적 필드들이 null인 경우에도 유효성 검사 통과")
    void passValidationWithOptionalFieldsNull() {
        // given
        CalendarEventRequest request = new CalendarEventRequest(
            "테스트 이벤트",
            null, // 설명 null
            validStartTime,
            validEndTime,
            null, // 장소 null
            null, // 카테고리 null
            null  // 알림 null
        );

        // when
        Set<ConstraintViolation<CalendarEventRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isEmpty();
    }
}