package talentos.pidev.controllers.quiz;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import talentos.pidev.models.Question;
import talentos.pidev.services.QuizService;

import java.sql.SQLException;
import java.util.Optional;

public class QuestionRowController {

    @FXML private Label enonceLabel;
    @FXML private Label nbChoixLabel;
    @FXML private Label correctLabel;
    @FXML private CheckBox publishedCheck;

    private Question question;
    private QuizQuestionsRHController parent;

    private final QuizService quizService = new QuizService();

    public void setData(Question q, QuizQuestionsRHController parent) {
        this.question = q;
        this.parent = parent;

        enonceLabel.setText(q.getEnonce());
        nbChoixLabel.setText("Choix: " + q.getNbChoix());
        correctLabel.setText("Correct: " + ((q.getCorrectPreview() == null || q.getCorrectPreview().isBlank()) ? "-" : q.getCorrectPreview()));

        // checkbox published
        publishedCheck.setSelected(q.isPublished());

        // éviter boucle lors refresh
        publishedCheck.selectedProperty().addListener((obs, oldV, newV) -> {
            try {
                quizService.setQuestionPublished(q.getId(), newV);
                q.setPublished(newV);
            } catch (SQLException e) {
                e.printStackTrace();
                publishedCheck.setSelected(oldV);
                new Alert(Alert.AlertType.ERROR, "Erreur publish: " + e.getMessage()).show();
            }
        });
    }

    @FXML
    private void onEdit() {
        if (parent != null) parent.openQuestionForm(question);
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
                if (parent != null) parent.reload();
            } catch (SQLException e) {
                e.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Erreur suppression: " + e.getMessage()).show();
            }
        }
    }
    @FXML
    private void onChoices() {
        if (parent != null && question != null) {
            parent.openChoices(question.getId(), question.getEnonce());
        }
    }
}