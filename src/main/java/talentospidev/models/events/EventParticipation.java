package talentospidev.models.events;

import java.time.LocalDateTime;

public class EventParticipation {
    private int id;
    private int eventId;
    private int userId;
    private String status; // CONFIRMED, PENDING, CANCELLED, ATTENDED
    private LocalDateTime registeredAt;
    private String qrCode;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(LocalDateTime registeredAt) { this.registeredAt = registeredAt; }
    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }
}
