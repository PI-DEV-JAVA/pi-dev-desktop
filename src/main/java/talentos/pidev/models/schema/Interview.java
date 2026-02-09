package talentos.pidev.models.schema;

import java.time.LocalDateTime;

public class Interview {
    private long id;
    private String title;
    private long recruiterId;
    private long candidateId;
    private String status;
    private Double generalGrade;
    private LocalDateTime createdAt;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public long getRecruiterId() { return recruiterId; }
    public void setRecruiterId(long recruiterId) { this.recruiterId = recruiterId; }

    public long getCandidateId() { return candidateId; }
    public void setCandidateId(long candidateId) { this.candidateId = candidateId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getGeneralGrade() { return generalGrade; }
    public void setGeneralGrade(Double generalGrade) { this.generalGrade = generalGrade; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}