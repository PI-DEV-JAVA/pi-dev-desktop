
        package talentos.pidev.models;

import java.time.LocalDate;

public class Application {
    private int id;
    private int offerId;
    private String candidateName;
    private String candidateEmail;
    private String candidatePhone;
    private String cvFilePath;
    private String motivationLetter;
    private String status;
    private LocalDate applicationDate;
    private double score;
    private String notes;
    private String interviewer;
    private LocalDate interviewDate;
    private String interviewResult;

    public Application() {}

    public Application(int offerId, String candidateName, String candidateEmail,
                       String candidatePhone, String cvFilePath, String motivationLetter) {
        this.offerId = offerId;
        this.candidateName = candidateName;
        this.candidateEmail = candidateEmail;
        this.candidatePhone = candidatePhone;
        this.cvFilePath = cvFilePath;
        this.motivationLetter = motivationLetter;
        this.status = "Nouvelle";
        this.applicationDate = LocalDate.now();
        this.score = 0;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOfferId() { return offerId; }
    public void setOfferId(int offerId) { this.offerId = offerId; }

    public String getCandidateName() { return candidateName; }
    public void setCandidateName(String candidateName) { this.candidateName = candidateName; }

    public String getCandidateEmail() { return candidateEmail; }
    public void setCandidateEmail(String candidateEmail) { this.candidateEmail = candidateEmail; }

    public String getCandidatePhone() { return candidatePhone; }
    public void setCandidatePhone(String candidatePhone) { this.candidatePhone = candidatePhone; }

    public String getCvFilePath() { return cvFilePath; }
    public void setCvFilePath(String cvFilePath) { this.cvFilePath = cvFilePath; }

    public String getMotivationLetter() { return motivationLetter; }
    public void setMotivationLetter(String motivationLetter) { this.motivationLetter = motivationLetter; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getApplicationDate() { return applicationDate; }
    public void setApplicationDate(LocalDate applicationDate) { this.applicationDate = applicationDate; }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getInterviewer() { return interviewer; }
    public void setInterviewer(String interviewer) { this.interviewer = interviewer; }

    public LocalDate getInterviewDate() { return interviewDate; }
    public void setInterviewDate(LocalDate interviewDate) { this.interviewDate = interviewDate; }

    public String getInterviewResult() { return interviewResult; }
    public void setInterviewResult(String interviewResult) { this.interviewResult = interviewResult; }
}
