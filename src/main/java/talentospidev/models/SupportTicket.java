package talentospidev.models;

import java.time.LocalDateTime;

public class SupportTicket {

    private int id;
    private int userId;
    private String userEmail;
    private String userFullName;
    private String subject;
    private String message;
    private String category; // BUG, QUESTION, FEATURE, OTHER
    private String status; // OPEN, IN_PROGRESS, RESOLVED, CLOSED
    private String priority; // LOW, MEDIUM, HIGH
    private LocalDateTime createdAt;
    private int replyCount;
    private int unreadCount;

    public SupportTicket() {
    }

    public SupportTicket(int userId, String subject, String message, String category) {
        this.userId = userId;
        this.subject = subject;
        this.message = message;
        this.category = category;
        this.status = "OPEN";
        this.priority = "MEDIUM";
    }

    // Getters & Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserFullName() {
        return userFullName;
    }

    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCategory() {
        return category != null ? category : "OTHER";
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStatus() {
        return status != null ? status : "OPEN";
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPriority() {
        return priority != null ? priority : "MEDIUM";
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public int getReplyCount() {
        return replyCount;
    }

    public void setReplyCount(int replyCount) {
        this.replyCount = replyCount;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }
}
