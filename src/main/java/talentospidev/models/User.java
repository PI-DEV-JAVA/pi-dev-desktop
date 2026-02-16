package talentospidev.models;

import java.time.LocalDateTime;

public class User {

    private int id;
    private String email;
    private String passwordHash;
    private Role role;
    private boolean active;
    private LocalDateTime createdAt;
    private AuthProvider authProvider = AuthProvider.LOCAL;
    private String providerId;



    // Roles in the system
    public enum Role {
        ADMIN,
        HR,
        CANDIDATE
    }
    public enum AuthProvider {
        LOCAL,
        GOOGLE
    }


    // Constructors
    public User() {
    }

    // Local user constructor
    public User(String email, String passwordHash, Role role) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.authProvider = AuthProvider.LOCAL;
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }
    // OAuth user constructor
    public User(String email, Role role, AuthProvider authProvider, String providerId) {
        this.email = email;
        this.role = role;
        this.authProvider = authProvider;
        this.providerId = providerId;
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }

    // Getters & Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public AuthProvider getAuthProvider() {
        return authProvider;
    }
    public void setAuthProvider(AuthProvider authProvider) {
        this.authProvider = authProvider;
    }
    public String getProviderId() {
        return providerId;
    }
    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }
}
