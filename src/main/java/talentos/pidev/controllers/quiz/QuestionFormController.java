package talentos.pidev.controllers.quiz;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import talentos.pidev.models.Question;
import talentos.pidev.services.QuizService;

import java.sql.SQLException;

public class QuestionFormController {

    @FXML private Label titleLabel;
    @FXML private TextArea enonceArea;
    @FXML private Label errorLabel;

    private int quizId;
    private Question questionToEdit;
    private Object parentController;
    private Runnable onDone;

    private final QuizService quizService = new QuizService();

    public void setQuizId(int quizId) { this.quizId = quizId; }
    public void setParentController(Object parentController) {
        this.parentController = parentController;
    }
    public void setQuestionToEdit(Question q) {
        this.questionToEdit = q;
        if (titleLabel != null) titleLabel.setText(q == null ? "Ajouter question" : "Modifier question");
        if (enonceArea != null) enonceArea.setText(q == null ? "" : q.getEnonce());
    }

    public void setOnDone(Runnable r) { this.onDone = r; }

    @FXML
    private void onSave() {
        if (errorLabel != null) errorLabel.setText("");

        String enonce = enonceArea.getText() == null ? "" : enonceArea.getText().trim();
        if (enonce.isEmpty()) {
            showError("Énoncé obligatoire");
            return;
        }

        try {
            if (questionToEdit == null) {
                Question q = new Question();
                q.setQuizId(quizId);
                q.setEnonce(enonce);
                quizService.addQuestion(q);
            } else {
                questionToEdit.setEnonce(enonce);
                quizService.updateQuestion(questionToEdit);
            }

            if (onDone != null) onDone.run();

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur sauvegarde: " + e.getMessage());
        }
    }

    @FXML
    private void onCancel() {
        if (onDone != null) onDone.run();
    }

    private void showError(String msg) {
        if (errorLabel != null) errorLabel.setText(msg);
        else new Alert(Alert.AlertType.ERROR, msg).show();
    }
}