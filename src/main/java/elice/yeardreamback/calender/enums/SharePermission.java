package elice.yeardreamback.calender.enums;

/**
 * 이벤트 공유 권한을 나타내는 열거형
 */
public enum SharePermission {
    VIEW_ONLY("읽기 전용"),
    EDIT("편집 가능");

    private final String description;

    SharePermission(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}