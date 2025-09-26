package elice.yeardreamback.enums;

public enum UserRoleType {
    ADMIN, COACH, USER;

    @Override
    public String toString() {
        return name();
    }
}
