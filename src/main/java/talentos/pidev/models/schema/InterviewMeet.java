package talentos.pidev.models.schema;

import java.time.LocalDateTime;

public class InterviewMeet {
    private long id;
    private long interviewId;
    private String uuid;
    private LocalDateTime scheduledAt;
    private String status;
    private Double grade;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getInterviewId() { return interviewId; }
    public void setInterviewId(long interviewId) { this.interviewId = interviewId; }
    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }
    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Double getGrade() { return grade; }
    public void setGrade(Double grade) { this.grade = grade; }
}