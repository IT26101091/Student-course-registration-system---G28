
package com.coursehub.model;

/**
 * Represents a user's login account.
 * Passwords are stored as hashes, never as plain text.
 */
public class Account {

    private String accountId;
    private String username;
    private String passwordHash;
    private String role;
    private String studentId;
    private boolean mustChangePassword;
    private boolean enabled = true;

    // Required for JSON serialization and deserialization.
    public Account() {
    }

    public Account(
            String accountId,
            String username,
            String passwordHash,
            String role,
            String studentId,
            boolean mustChangePassword
    ) {
        this.accountId = accountId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.studentId = studentId;
        this.mustChangePassword = mustChangePassword;
        this.enabled = true;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public boolean isMustChangePassword() {
        return mustChangePassword;
    }

    public void setMustChangePassword(
            boolean mustChangePassword
    ) {
        this.mustChangePassword = mustChangePassword;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
