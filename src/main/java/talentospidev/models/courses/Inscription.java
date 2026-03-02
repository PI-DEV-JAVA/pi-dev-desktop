package talentospidev.models.courses;

import java.time.LocalDateTime;

public class Inscription {
    private int id;
    private int formationId;
    private int userId;
    private String candidatNom;
    private String candidatEmail;
    private LocalDateTime dateInscription;
    private String statut;        // EN_ATTENTE, ACCEPTEE, REFUSEE
    private Double scoreQuiz;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getFormationId() { return formationId; }
    public void setFormationId(int formationId) { this.formationId = formationId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getCandidatNom() { return candidatNom; }
    public void setCandidatNom(String candidatNom) { this.candidatNom = candidatNom; }

    public String getCandidatEmail() { return candidatEmail; }
    public void setCandidatEmail(String candidatEmail) { this.candidatEmail = candidatEmail; }

    public LocalDateTime getDateInscription() { return dateInscription; }
    public void setDateInscription(LocalDateTime dateInscription) { this.dateInscription = dateInscription; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public Double getScoreQuiz() { return scoreQuiz; }
    public void setScoreQuiz(Double scoreQuiz) { this.scoreQuiz = scoreQuiz; }
}
