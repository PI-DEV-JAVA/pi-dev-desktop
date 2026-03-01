package talentos.pidev.models;

import java.time.LocalDateTime;

public class TentativeQuiz {

    private int id;
    private int quizId;

    private String candidatNom;      // ✅ exists in DB
    private String candidatEmail;

    private int score;
    private int total;

    private LocalDateTime createdAt; // ✅ exists in DB

    public TentativeQuiz() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getQuizId() { return quizId; }
    public void setQuizId(int quizId) { this.quizId = quizId; }

    public String getCandidatNom() { return candidatNom; }
    public void setCandidatNom(String candidatNom) { this.candidatNom = candidatNom; }

    public String getCandidatEmail() { return candidatEmail; }
    public void setCandidatEmail(String candidatEmail) { this.candidatEmail = candidatEmail; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "TentativeQuiz{" +
                "id=" + id +
                ", quizId=" + quizId +
                ", nom='" + candidatNom + '\'' +
                ", email='" + candidatEmail + '\'' +
                ", score=" + score + "/" + total +
                ", createdAt=" + createdAt +
                '}';
    }
}