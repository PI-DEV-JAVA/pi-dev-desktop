package talentos.pidev.models;

import java.time.LocalDateTime;

public class Inscription {
    private int id;
    private int formationId;
    private String candidatNom;
    private String candidatEmail;
    private LocalDateTime dateInscription;
    private String statut;
    private Double scoreQuiz;
    private LocalDateTime createdAt;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getFormationId() { return formationId; }
    public void setFormationId(int formationId) { this.formationId = formationId; }

    public String getCandidatNom() { return candidatNom; }
    public void setCandidatNom(String candidatNom) { this.candidatNom = candidatNom; }

    public String getCandidatEmail() { return candidatEmail; }
    public void setCandidatEmail(String candidatEmail) { this.candidatEmail = candidatEmail; }

    public LocalDateTime getDateInscription() { return dateInscription; }
    public void setDateInscription(LocalDateTime dateInscription) { this.dateInscription = dateInscription; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Double getScoreQuiz() { return scoreQuiz; }
    public void setScoreQuiz(Double scoreQuiz) { this.scoreQuiz = scoreQuiz; }
}
