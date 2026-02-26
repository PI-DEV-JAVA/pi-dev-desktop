package talentos.pidev.models;

import java.time.LocalDateTime;

public class TentativeQuiz {
    private int id;
    private int quizId;
    private String candidatEmail;
    private int score;
    private int total;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;

    public TentativeQuiz() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getQuizId() { return quizId; }
    public void setQuizId(int quizId) { this.quizId = quizId; }

    public String getCandidatEmail() { return candidatEmail; }
    public void setCandidatEmail(String candidatEmail) { this.candidatEmail = candidatEmail; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getFinishedAt() { return finishedAt; }
    public void setFinishedAt(LocalDateTime finishedAt) { this.finishedAt = finishedAt; }

    @Override
    public String toString() {
        return "TentativeQuiz{id=" + id + ", quizId=" + quizId + ", email='" + candidatEmail + "', score=" + score + "/" + total + "}";
    }
}
