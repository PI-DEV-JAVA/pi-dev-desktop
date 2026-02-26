package talentos.pidev.controllers.quiz;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import talentos.pidev.models.Choix;
import talentos.pidev.models.Question;
import talentos.pidev.models.TentativeQuiz;
import talentos.pidev.services.QuizService;
import talentos.pidev.services.TentativeService;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

public class QuizPassageController {

    @FXML private Label lbQuestion;
    @FXML private ToggleGroup tgChoices;
    @FXML private RadioButton rb1;
    @FXML private RadioButton rb2;
    @FXML private RadioButton rb3;
    @FXML private RadioButton rb4;
    @FXML private Button btnNext;

    private int quizId;
    private String candidatEmail;

    private final QuizService quizService = new QuizService();
    private final TentativeService tentativeService = new TentativeService();

    private List<Question> questions = new ArrayList<>();
    private int index = 0;
    private int score = 0;

    private final Map<Integer, List<Choix>> cacheChoices = new HashMap<>();
    private TentativeQuiz tentative;

    public void start(int quizId, String candidatEmail) {
        this.quizId = quizId;
        this.candidatEmail = candidatEmail;

        try {
            questions = quizService.getQuestionsByQuiz(quizId);
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur chargement questions: " + e.getMessage()).show();
            return;
        }

        index = 0;
        score = 0;

        tentative = new TentativeQuiz();
        tentative.setQuizId(quizId);
        tentative.setCandidatEmail(candidatEmail);
        tentative.setStartedAt(LocalDateTime.now());

        showQuestion();
    }

    private void showQuestion() {
        if (questions == null || questions.isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION, "Aucune question dans ce quiz.").show();
            return;
        }

        Question q = questions.get(index);
        lbQuestion.setText((index + 1) + ") " + q.getEnonce());

        List<Choix> list = cacheChoices.get(q.getId());
        if (list == null) {
            try {
                list = quizService.getChoicesByQuestion(q.getId());
                cacheChoices.put(q.getId(), list);
            } catch (SQLException e) {
                e.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Erreur chargement choix: " + e.getMessage()).show();
                return;
            }
        }

        tgChoices.selectToggle(null);
        rb1.setVisible(false); rb2.setVisible(false); rb3.setVisible(false); rb4.setVisible(false);

        RadioButton[] arr = {rb1, rb2, rb3, rb4};
        for (int i = 0; i < arr.length; i++) {
            if (i < list.size()) {
                arr[i].setVisible(true);
                arr[i].setText(list.get(i).getTexte());
                arr[i].setUserData(list.get(i));
            }
        }

        btnNext.setText(index == questions.size() - 1 ? "Terminer" : "Suivant");
    }

    @FXML
    private void onNext() {
        Toggle selected = tgChoices.getSelectedToggle();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Choisis une réponse.").show();
            return;
        }

        Choix selectedChoice = (Choix) selected.getUserData();
        if (selectedChoice != null && selectedChoice.isEstCorrect()) score++;

        if (index < questions.size() - 1) {
            index++;
            showQuestion();
        } else {
            tentative.setFinishedAt(LocalDateTime.now());
            tentative.setScore(score);
            tentative.setTotal(questions.size());

            try {
                tentativeService.saveTentative(tentative);
            } catch (SQLException e) {
                e.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Erreur sauvegarde tentative: " + e.getMessage()).show();
                return;
            }

            new Alert(Alert.AlertType.INFORMATION, "Quiz terminé ✅\nScore: " + score + " / " + questions.size()).show();
        }
    }
}
