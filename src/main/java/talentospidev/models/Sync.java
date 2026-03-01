package talentospidev.models;

import java.time.LocalDateTime;

public class Sync {

    private int id;
    private int senderId;
    private int receiverId;
    private String senderName;
    private String receiverName;
    private String senderTitle;
    private String receiverTitle;
    private String senderRole;
    private String receiverRole;
    private String reason; // COLLABORATE, LEARN, MENTOR, HIRE, NETWORK
    private String status; // PENDING, ACCEPTED, DECLINED
    private LocalDateTime createdAt;
    private LocalDateTime acceptedAt;

    public Sync() {
    }

    // Getters & Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(int receiverId) {
        this.receiverId = receiverId;
    }

    public String getSenderName() {
        return senderName != null ? senderName : "Unknown";
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getReceiverName() {
        return receiverName != null ? receiverName : "Unknown";
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getSenderTitle() {
        return senderTitle != null ? senderTitle : "";
    }

    public void setSenderTitle(String senderTitle) {
        this.senderTitle = senderTitle;
    }

    public String getReceiverTitle() {
        return receiverTitle != null ? receiverTitle : "";
    }

    public void setReceiverTitle(String receiverTitle) {
        this.receiverTitle = receiverTitle;
    }

    public String getSenderRole() {
        return senderRole != null ? senderRole : "";
    }

    public void setSenderRole(String senderRole) {
        this.senderRole = senderRole;
    }

    public String getReceiverRole() {
        return receiverRole != null ? receiverRole : "";
    }

    public void setReceiverRole(String receiverRole) {
        this.receiverRole = receiverRole;
    }

    public String getReason() {
        return reason != null ? reason : "NETWORK";
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status != null ? status : "PENDING";
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(LocalDateTime acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    /** Get the "other" user's ID relative to the given user. */
    public int getOtherUserId(int myId) {
        return myId == senderId ? receiverId : senderId;
    }

    public String getOtherUserName(int myId) {
        return myId == senderId ? receiverName : senderName;
    }

    public String getOtherUserTitle(int myId) {
        return myId == senderId ? receiverTitle : senderTitle;
    }

    public String getReasonEmoji() {
        return switch (getReason()) {
            case "COLLABORATE" -> "🤝";
            case "LEARN" -> "📚";
            case "MENTOR" -> "🎓";
            case "HIRE" -> "💼";
            default -> "🌐";
        };
    }

    public String getReasonLabel() {
        return switch (getReason()) {
            case "COLLABORATE" -> "Collaborator";
            case "LEARN" -> "Learner";
            case "MENTOR" -> "Mentor";
            case "HIRE" -> "Recruiter";
            default -> "Network";
        };
    }
}
