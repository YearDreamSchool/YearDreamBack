package elice.yeardreamback.entity;

public enum UserRoleType {
    ADMIN, COACH, USER;

    @Override
    public String toString() {
        return name();
    }
}
