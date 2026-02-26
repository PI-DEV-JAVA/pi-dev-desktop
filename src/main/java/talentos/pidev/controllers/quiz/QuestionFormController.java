package talentos.pidev.controllers.quiz;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import talentos.pidev.models.Question;
import talentos.pidev.services.QuizService;

import java.sql.SQLException;

public class QuestionFormController {

    @FXML private Label titleLabel;
    @FXML private TextArea enonceArea;
    @FXML private Label errorLabel;

    private int quizId;
    private Question questionToEdit;

    private QuizQuestionsRHController parentQuizQuestions;
    private QuestionsRHController parentOld;

    private Runnable onDone; // ✅ unique callback

    private final QuizService quizService = new QuizService();

    public void setQuizId(int quizId) { this.quizId = quizId; }

    public void setOnDone(Runnable r) { this.onDone = r; }

    public void setParentController(QuizQuestionsRHController parentController) {
        this.parentQuizQuestions = parentController;
        this.parentOld = null;
    }

    public void setParentController(QuestionsRHController parentController) {
        this.parentOld = parentController;
        this.parentQuizQuestions = null;
    }

    public void setQuestionToEdit(Question q) {
        this.questionToEdit = q;
        if (titleLabel != null) {
            titleLabel.setText(q != null ? "Modifier question" : "Ajouter question");
        }
        if (enonceArea != null) {
            enonceArea.setText(q != null ? q.getEnonce() : "");
        }
    }

    @FXML
    private void onSave() {
        String enonce = (enonceArea.getText() == null) ? "" : enonceArea.getText().trim();
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

            // ✅ refresh parent
            if (parentQuizQuestions != null) parentQuizQuestions.reload();
            else if (parentOld != null) parentOld.refresh();

            // ✅ retour (MainLayout)
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