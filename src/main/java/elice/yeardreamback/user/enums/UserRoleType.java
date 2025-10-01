package elice.yeardreamback.user.enums;

/**
 * 시스템 내 사용자 권한 유형을 정의하는 Enum 클래스입니다.
 */
public enum UserRoleType {
    /**
     * ADMIN: 시스템 관리자 권한을 가진 운영진을 의미합니다.
     * COACH: 코치 권한을 가진 코치를 의미합니다.
     * USER: 일반 사용자 권한을 가진 교육생을 의미합니다.
     */
    ADMIN, COACH, USER;

    /**
     * Enum 상수의 이름을 문자열로 반환합니다.
     * 데이터베이스 저장이나 문자열 비교 시 사용될 수 있습니다.
     * @return Enum 상수의 이름 (예: "ADMIN", "COACH", "USER")
     */
    @Override
    public String toString() {
        return name();
    }
}
