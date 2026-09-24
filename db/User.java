package db;

/**
 * User session model for authenticated accounts.
 */
public class User {
    private final String username;
    private final String role;
    private final String fullName;

    public User(String username, String role, String fullName) {
        this.username = username;
        this.role = role;
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public String getFullName() {
        return fullName;
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    @Override
    public String toString() {
        return fullName + " (" + role + ")";
    }
}
