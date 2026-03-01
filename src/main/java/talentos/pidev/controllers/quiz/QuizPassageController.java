package talentos.pidev.controllers.quiz;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import talentos.pidev.models.Choix;
import talentos.pidev.models.Question;
import talentos.pidev.models.TentativeQuiz;
import talentos.pidev.services.QuizService;
import talentos.pidev.services.TentativeService;

import java.sql.SQLException;
import java.util.*;
import java.util.function.BiConsumer;

public class QuizPassageController {

    @FXML private Label lbQuizTitle;
    @FXML private Label lbCounter;
    @FXML private Label lbTimer;
    @FXML private Label lbQuestion;

    @FXML private ProgressBar progressBar;

    @FXML private ToggleGroup tgChoices;
    @FXML private ToggleButton opt1;
    @FXML private ToggleButton opt2;
    @FXML private ToggleButton opt3;
    @FXML private ToggleButton opt4;

    @FXML private Button btnPrev;
    @FXML private Button btnNext;

    private int quizId;
    private String quizTitle;
    private String candidatEmail;

    private final QuizService quizService = new QuizService();
    private final TentativeService tentativeService = new TentativeService();

    private List<Question> questions = new ArrayList<>();
    private int index = 0;
    private int score = 0;

    private final Map<Integer, List<Choix>> cacheChoices = new HashMap<>();
    private TentativeQuiz tentative;

    private final Map<Integer, Choix> selectedByQuestion = new HashMap<>();

    private ToggleButton[] opts;
    private Runnable onExit;

    // ✅ NEW: send result back
    private BiConsumer<Integer, Integer> onFinished;

    public void setOnFinished(BiConsumer<Integer, Integer> onFinished) {
        this.onFinished = onFinished;
    }

    @FXML
    public void initialize() {
        opts = new ToggleButton[]{opt1, opt2, opt3, opt4};

        for (ToggleButton b : opts) {
            b.setWrapText(true);
            b.setMaxWidth(Double.MAX_VALUE);
        }

        btnPrev.setDisable(true);
        progressBar.setProgress(0);
    }

    public void start(int quizId, String quizTitle, String candidatEmail) {
        this.quizId = quizId;
        this.quizTitle = quizTitle;
        this.candidatEmail = candidatEmail;

        try {
            questions = quizService.getQuestionsByQuiz(quizId);
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur chargement questions: " + e.getMessage()).show();
            return;
        }

        if (lbQuizTitle != null) lbQuizTitle.setText(quizTitle == null ? "Quiz" : quizTitle);

        index = 0;
        score = 0;
        selectedByQuestion.clear();

        tentative = new TentativeQuiz();
        tentative.setQuizId(quizId);
        tentative.setCandidatEmail(candidatEmail);
        tentative.setCandidatNom("Test Candidat");

        showQuestion();
    }

    private void showQuestion() {
        if (questions == null || questions.isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION, "Aucune question dans ce quiz.").show();
            return;
        }

        Question q = questions.get(index);

        lbCounter.setText("Question " + (index + 1) + " / " + questions.size());
        progressBar.setProgress((index + 1) * 1.0 / questions.size());

        lbQuestion.setText(q.getEnonce());

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
        for (ToggleButton b : opts) {
            b.setVisible(false);
            b.setManaged(false);
            b.setUserData(null);
            b.setText("");
        }

        for (int i = 0; i < opts.length; i++) {
            if (i < list.size()) {
                ToggleButton b = opts[i];
                Choix ch = list.get(i);

                b.setVisible(true);
                b.setManaged(true);
                b.setText(ch.getTexte());
                b.setUserData(ch);
            }
        }

        Choix prev = selectedByQuestion.get(q.getId());
        if (prev != null) {
            for (ToggleButton b : opts) {
                if (b.isVisible() && b.getUserData() instanceof Choix c && c.getId() == prev.getId()) {
                    tgChoices.selectToggle(b);
                    break;
                }
            }
        }

        btnPrev.setDisable(index == 0);
        btnNext.setText(index == questions.size() - 1 ? "Terminer" : "Suivant");
    }

    @FXML
    private void onNext() {
        Toggle selected = tgChoices.getSelectedToggle();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Choisis une réponse.").show();
            return;
        }

        Question q = questions.get(index);
        Choix selectedChoice = (Choix) selected.getUserData();
        selectedByQuestion.put(q.getId(), selectedChoice);

        if (index < questions.size() - 1) {
            index++;
            showQuestion();
        } else {
            finishQuiz();
        }
    }

    @FXML
    private void onPrev() {
        if (index > 0) {
            index--;
            showQuestion();
        }
    }

    private void finishQuiz() {
        int s = 0;
        for (Question q : questions) {
            Choix c = selectedByQuestion.get(q.getId());
            if (c != null && c.isEstCorrect()) s++;
        }
        score = s;

        tentative.setScore(score);
        tentative.setTotal(questions.size());

        try {
            tentativeService.saveTentative(tentative);
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur sauvegarde tentative: " + e.getMessage()).show();
            return;
        }

        // ✅ notify parent page (seance)
        if (onFinished != null) onFinished.accept(score, questions.size());

        new Alert(Alert.AlertType.INFORMATION,
                "Quiz terminé ✅\nScore: " + score + " / " + questions.size()).showAndWait();

        if (onExit != null) onExit.run();
    }

    public void setOnExit(Runnable r) { this.onExit = r; }

    @FXML
    private void onQuit() {
        if (onExit != null) onExit.run();
    }
}