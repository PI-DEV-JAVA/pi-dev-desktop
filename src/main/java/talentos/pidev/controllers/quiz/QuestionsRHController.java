package talentos.pidev.controllers.quiz;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.layout.FlowPane;
import talentos.pidev.models.Question;
import talentos.pidev.services.QuizService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class QuestionsRHController {

    @FXML private FlowPane fpQuestions;

    private int quizId;
    private final QuizService quizService = new QuizService();

    public void setQuizId(int quizId) {
        this.quizId = quizId;
        refresh();
    }

    public void refresh() {
        fpQuestions.getChildren().clear();
        try {
            List<Question> questions = quizService.getQuestionsByQuiz(quizId);
            for (Question q : questions) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/QuestionCard.fxml"));
                Parent card = loader.load();
                QuestionCardController c = loader.getController();
                c.setData(q, this);
                fpQuestions.getChildren().add(card);
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur chargement questions: " + e.getMessage()).show();
        }
    }

    public void openForm(Question toEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/QuestionForm.fxml"));
            Parent root = loader.load();
            QuestionFormController c = loader.getController();
            c.setParentController(this);
            c.setQuizId(quizId);
            c.setQuestionToEdit(toEdit);
            fpQuestions.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur ouverture form: " + e.getMessage()).show();
        }
    }
}
