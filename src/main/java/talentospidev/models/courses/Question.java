package talentospidev.models.courses;

public class Question {
    private int id;
    private int quizId;
    private String enonce;
    private int points;
    private int ordre;
    private boolean published;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getQuizId() { return quizId; }
    public void setQuizId(int quizId) { this.quizId = quizId; }

    public String getEnonce() { return enonce; }
    public void setEnonce(String enonce) { this.enonce = enonce; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    public int getOrdre() { return ordre; }
    public void setOrdre(int ordre) { this.ordre = ordre; }

    public boolean isPublished() { return published; }
    public void setPublished(boolean published) { this.published = published; }
}
