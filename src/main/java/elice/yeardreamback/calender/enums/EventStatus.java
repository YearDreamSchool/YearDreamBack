package elice.yeardreamback.calender.enums;

/**
 * 캘린더 이벤트의 상태를 나타내는 열거형
 */
public enum EventStatus {
    SCHEDULED("예정됨"),
    IN_PROGRESS("진행중"),
    COMPLETED("완료됨"),
    CANCELLED("취소됨");

    private final String description;

    EventStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}