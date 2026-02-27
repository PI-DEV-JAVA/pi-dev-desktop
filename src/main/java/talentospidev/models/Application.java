
package talentospidev.models;

import java.time.LocalDate;

public class Application {
    private int id;
    private int userId;
    private int offerId;
    private String cvFilePath;
    private String motivationLetter;
    private String status;
    private LocalDate applicationDate;
    private double score;
    private String notes;
    private String interviewer;
    private LocalDate interviewDate;
    private String interviewResult;
    private String recruiterResponse;
    private LocalDate responseDate;

    public Application() {
    }

    public Application(int userId, int offerId, String cvFilePath, String motivationLetter) {
        this.userId = userId;
        this.offerId = offerId;
        this.cvFilePath = cvFilePath;
        this.motivationLetter = motivationLetter;
        this.status = "Nouvelle";
        this.applicationDate = LocalDate.now();
        this.score = 0;
    }

    // Getters et Setters
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

    public int getOfferId() {
        return offerId;
    }

    public void setOfferId(int offerId) {
        this.offerId = offerId;
    }

    public String getCvFilePath() {
        return cvFilePath;
    }

    public void setCvFilePath(String cvFilePath) {
        this.cvFilePath = cvFilePath;
    }

    public String getMotivationLetter() {
        return motivationLetter;
    }

    public void setMotivationLetter(String motivationLetter) {
        this.motivationLetter = motivationLetter;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getApplicationDate() {
        return applicationDate;
    }

    public void setApplicationDate(LocalDate applicationDate) {
        this.applicationDate = applicationDate;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getInterviewer() {
        return interviewer;
    }

    public void setInterviewer(String interviewer) {
        this.interviewer = interviewer;
    }

    public LocalDate getInterviewDate() {
        return interviewDate;
    }

    public void setInterviewDate(LocalDate interviewDate) {
        this.interviewDate = interviewDate;
    }

    public String getInterviewResult() {
        return interviewResult;
    }

    public void setInterviewResult(String interviewResult) {
        this.interviewResult = interviewResult;
    }

    public String getRecruiterResponse() {
        return recruiterResponse;
    }

    public void setRecruiterResponse(String recruiterResponse) {
        this.recruiterResponse = recruiterResponse;
    }

    public LocalDate getResponseDate() {
        return responseDate;
    }

    public void setResponseDate(LocalDate responseDate) {
        this.responseDate = responseDate;
    }

    /**
     * Whether the recruiter has responded to this application.
     */
    public boolean hasResponse() {
        return recruiterResponse != null && !recruiterResponse.isEmpty();
    }
}
