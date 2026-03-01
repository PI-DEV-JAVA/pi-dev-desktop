package talentospidev.models;

import java.time.LocalDateTime;

public class SyncMessage {

    private int id;
    private int syncId;
    private int senderId;
    private String senderName;
    private String message;
    private boolean isRead;
    private LocalDateTime createdAt;

    // For chat list: last message preview
    private String otherUserName;
    private int otherUserId;
    private int unreadCount;

    public SyncMessage() {
    }

    public SyncMessage(int syncId, int senderId, String message) {
        this.syncId = syncId;
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

    public int getSyncId() {
        return syncId;
    }

    public void setSyncId(int syncId) {
        this.syncId = syncId;
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

    public String getOtherUserName() {
        return otherUserName != null ? otherUserName : "Unknown";
    }

    public void setOtherUserName(String otherUserName) {
        this.otherUserName = otherUserName;
    }

    public int getOtherUserId() {
        return otherUserId;
    }

    public void setOtherUserId(int otherUserId) {
        this.otherUserId = otherUserId;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }
}
