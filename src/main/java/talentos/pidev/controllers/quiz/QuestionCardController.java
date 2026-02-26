package talentos.pidev.controllers.quiz;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import talentos.pidev.models.Question;
import talentos.pidev.services.QuizService;

import java.sql.SQLException;
import java.util.Optional;

public class QuestionCardController {

    @FXML private Label ordreLabel;
    @FXML private Label pointsLabel;
    @FXML private Label nbChoixLabel;

    @FXML private Label enonceLabel;
    @FXML private Label correctLabel;

    private Question question;

    // ✅ Parent nouveau (page Questions d’un Quiz)
    private QuizQuestionsRHController parentQuizQuestions;

    // (Optionnel) si tu as encore un ancien controller, tu peux garder ceci
    private QuestionsRHController parentOld;

    private final QuizService quizService = new QuizService();

    // ===================== Nouveau parent =====================
    public void setData(Question q, QuizQuestionsRHController parent) {
        this.question = q;
        this.parentQuizQuestions = parent;
        this.parentOld = null;
        fillUI(q);
    }

    // ===================== Ancien parent (si existant) =====================
    public void setData(Question q, QuestionsRHController parent) {
        this.question = q;
        this.parentOld = parent;
        this.parentQuizQuestions = null;
        fillUI(q);
    }

    private void fillUI(Question q) {
        // Certains champs peuvent être absents selon ton modèle/table, donc on sécurise
        if (ordreLabel != null) ordreLabel.setText("#" + (q.getId())); // ou q.getOrdre() si tu as
        if (pointsLabel != null) pointsLabel.setText("1 pt");         // ou q.getPoints() si tu as
        if (nbChoixLabel != null) nbChoixLabel.setText("Choix: " + q.getNbChoix());

        if (enonceLabel != null) enonceLabel.setText(q.getEnonce());

        String correct = q.getCorrectPreview();
        if (correctLabel != null) {
            correctLabel.setText("Bonne réponse: " + (correct == null || correct.isBlank() ? "-" : correct));
        }
    }

    // ===================== Actions UI =====================

    @FXML
    private void onEdit() {
        if (parentQuizQuestions != null) parentQuizQuestions.openQuestionForm(question);
        else if (parentOld != null) parentOld.openForm(question);
    }

    @FXML
    private void onDelete() {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Suppression");
        a.setHeaderText("Supprimer cette question ?");
        a.setContentText(question.getEnonce());

        Optional<ButtonType> res = a.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            try {
                quizService.deleteQuestion(question.getId());

                if (parentQuizQuestions != null) parentQuizQuestions.reload();
                else if (parentOld != null) parentOld.refresh();

            } catch (SQLException e) {
                e.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Erreur suppression: " + e.getMessage()).show();
            }
        }
    }
/*
    @FXML
    private void onChoix() {
        if (parentQuizQuestions != null) {
            parentQuizQuestions.openChoices(question.getId(), question.getEnonce());
        } else if (parentOld != null) {
            // si tu avais ancien écran choix
            // parentOld.openChoices(question.getId());
        }
    }

 */
}
