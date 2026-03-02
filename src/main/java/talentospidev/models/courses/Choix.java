package talentospidev.models.courses;

public class Choix {
    private int id;
    private int questionId;
    private String texte;
    private boolean estCorrect;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getQuestionId() { return questionId; }
    public void setQuestionId(int questionId) { this.questionId = questionId; }

    public String getTexte() { return texte; }
    public void setTexte(String texte) { this.texte = texte; }

    public boolean isEstCorrect() { return estCorrect; }
    public void setEstCorrect(boolean estCorrect) { this.estCorrect = estCorrect; }
}
