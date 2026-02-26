package talentos.pidev.models;

public class Question {
    private int id;
    private int quizId;
    private String enonce;
    private int nbChoix;              // utilisé par UI
    private String correctPreview;    // optionnel (affichage admin)
    private boolean published;

    public Question() {}

    public Question(int id, int quizId, String enonce) {
        this.id = id;
        this.quizId = quizId;
        this.enonce = enonce;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getQuizId() { return quizId; }
    public void setQuizId(int quizId) { this.quizId = quizId; }

    public String getEnonce() { return enonce; }
    public void setEnonce(String enonce) { this.enonce = enonce; }

    public int getNbChoix() { return nbChoix; }
    public void setNbChoix(int nbChoix) { this.nbChoix = nbChoix; }

    public String getCorrectPreview() { return correctPreview; }
    public void setCorrectPreview(String correctPreview) { this.correctPreview = correctPreview; }

    public boolean isPublished() { return published; }
    public void setPublished(boolean published) { this.published = published; }

    @Override
    public String toString() {
        return "Question{id=" + id + ", quizId=" + quizId + ", enonce='" + enonce + "', nbChoix=" + nbChoix + "}";
    }
}
