package talentos.pidev.models;
import java.time.LocalDateTime;

public class Choix {
    private int id;
    private int questionId;
    private String texte;
    private boolean estCorrect;
    private LocalDateTime createdAt;


    public Choix() {}

    public Choix(int id, int questionId, String texte, boolean estCorrect) {
        this.id = id;
        this.questionId = questionId;
        this.texte = texte;
        this.estCorrect = estCorrect;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getQuestionId() { return questionId; }
    public void setQuestionId(int questionId) { this.questionId = questionId; }

    public String getTexte() { return texte; }
    public void setTexte(String texte) { this.texte = texte; }

    public boolean isEstCorrect() { return estCorrect; }
    public void setEstCorrect(boolean correct) { this.estCorrect = correct; }

    // Alias UI (certains controllers/FXML utilisaient "contenu")
    public String getContenu() { return texte; }
    public void setContenu(String contenu) { this.texte = contenu; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Choix{id=" + id + ", questionId=" + questionId + ", texte='" + texte + "', correct=" + estCorrect + "}";
    }
}
