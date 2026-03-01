package talentospidev.models;

import java.time.LocalDateTime;

public class TicketReply {

    private int id;
    private int ticketId;
    private int senderId;
    private String senderName;
    private String senderRole;
    private String message;
    private boolean isRead;
    private LocalDateTime createdAt;

    public TicketReply() {
    }

    public TicketReply(int ticketId, int senderId, String message) {
        this.ticketId = ticketId;
        this.senderId = senderId;
        this.message = message;
    }

    // Getters & Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTicketId() {
        return ticketId;
    }

    public void setTicketId(int ticketId) {
        this.ticketId = ticketId;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName != null ? senderName : "Unknown";
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderRole() {
        return senderRole != null ? senderRole : "";
    }

    public void setSenderRole(String senderRole) {
        this.senderRole = senderRole;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
