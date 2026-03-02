package talentospidev.controllers.courses;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import talentospidev.dao.coursesDAO.*;
import talentospidev.models.User;
import talentospidev.models.courses.*;
import talentospidev.services.AuthService;
import talentospidev.utils.SceneUtil;
import talentospidev.utils.ViewContext;

import java.util.*;

public class QuizPassageController {

    @FXML private Label quizTitleLbl, timerLbl, progressInfoLbl;
    @FXML private VBox questionsContainer, progressBarContainer, sidebar;
    @FXML private ProgressBar quizProgressBar;
    @FXML private Button submitBtn;
    @FXML private Button toDoTab, activitiesTab, projectsTab;

    private final QuizDAO quizDAO = new QuizDAO();
    private final QuestionDAO questionDAO = new QuestionDAO();
    private final ChoixDAO choixDAO = new ChoixDAO();
    private final TentativeDAO tentativeDAO = new TentativeDAO();

    private Quiz quiz;
    private List<Question> questions;
    private final Map<Integer, ToggleGroup> answerGroups = new LinkedHashMap<>();
    private final Map<Integer, Integer> selectedChoices = new HashMap<>();

    // =========================
    // ✅ ANTI-CHEAT (Focus/Minimize)
    // =========================
    private Stage quizStage;
    private int focusLostCount = 0;
    private boolean quizSubmitted = false;

    // change ça comme tu veux
    private static final int MAX_FOCUS_LOST = 2;

    @FXML
    public void initialize() {
        talentospidev.utils.SidebarUtil.configure(toDoTab, activitiesTab, projectsTab);

        // ✅ Attacher anti-cheat dès que la Scene/Window est prête
        Platform.runLater(this::attachAntiCheatToCurrentWindow);

        int qId = ViewContext.getSelectedQuizId();
        if (qId > 0) {
            try {
                quiz = quizDAO.getById(qId);
                if (quiz != null) {
                    quizTitleLbl.setText("📝 " + quiz.getTitre());
                    timerLbl.setText("⏱ " + quiz.getDureeMinutes() + " min");
                    loadQuestions();
                }
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void attachAntiCheatToCurrentWindow() {
        // si le root est déjà attaché à une scène, on récupère la window
        if (questionsContainer == null || questionsContainer.getScene() == null) return;
        if (questionsContainer.getScene().getWindow() == null) return;

        quizStage = (Stage) questionsContainer.getScene().getWindow();

        // 1) perte de focus (Alt+Tab / clic ailleurs)
        quizStage.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) onFocusLost("Fenêtre quittée (Alt+Tab / clic ailleurs).");
        });

        // 2) minimisation
        quizStage.iconifiedProperty().addListener((obs, wasIconified, isIconified) -> {
            if (isIconified) onFocusLost("Fenêtre minimisée.");
        });
    }

    private void onFocusLost(String reason) {
        // si déjà soumis => ne rien faire
        if (quizSubmitted) return;

        focusLostCount++;

        // Optionnel : afficher dans timerLbl (sans casser ton timer)
        // timerLbl.setText("⏱ " + quiz.getDureeMinutes() + " min  |  ⚠ " + focusLostCount + "/" + MAX_FOCUS_LOST);

        Platform.runLater(() -> {
            // warning
            if (focusLostCount < MAX_FOCUS_LOST) {
                Alert a = new Alert(Alert.AlertType.WARNING);
                a.setTitle("Anti-triche");
                a.setHeaderText("Attention");
                a.setContentText(reason + "\nTentatives: " + focusLostCount + " / " + MAX_FOCUS_LOST);
                a.showAndWait();
                return;
            }

            // sanction: auto-submit
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Anti-triche");
            a.setHeaderText("Quiz soumis automatiquement");
            a.setContentText("Tu as quitté le quiz trop de fois.\nLe quiz sera soumis automatiquement.");
            a.showAndWait();

            // ✅ auto submit (force)
            onSubmitForce();
        });
    }

    private void loadQuestions() throws Exception {
        questionsContainer.getChildren().clear();
        questions = questionDAO.getByQuizId(quiz.getId());
        int total = questions.size();
        progressInfoLbl.setText("Answer all " + total + " questions below");
        quizProgressBar.setProgress(0);

        int idx = 1;
        for (Question q : questions) {
            VBox card = new VBox(10);
            card.setPadding(new Insets(18, 22, 18, 22));
            card.setStyle("-fx-background-color: white; -fx-background-radius: 14; " +
                    "-fx-border-color: #e5e7eb; -fx-border-radius: 14; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.03), 4, 0, 0, 1);");

            HBox qHeader = new HBox(10);
            qHeader.setAlignment(Pos.CENTER_LEFT);
            Label numLbl = new Label("Q" + idx);
            numLbl.setStyle("-fx-background-color: #6366f1; -fx-text-fill: white; -fx-background-radius: 8; " +
                    "-fx-padding: 4 10; -fx-font-size: 12px; -fx-font-weight: 700;");
            Label qLbl = new Label(q.getEnonce());
            qLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: 700; -fx-text-fill: #111827;");
            qLbl.setWrapText(true);
            qHeader.getChildren().addAll(numLbl, qLbl);
            card.getChildren().add(qHeader);

            ToggleGroup tg = new ToggleGroup();
            answerGroups.put(q.getId(), tg);

            List<Choix> choices = choixDAO.getByQuestionId(q.getId());
            VBox choicesBox = new VBox(6);
            choicesBox.setPadding(new Insets(4, 0, 0, 34));

            for (Choix c : choices) {
                RadioButton rb = new RadioButton(c.getTexte());
                rb.setToggleGroup(tg);
                rb.setUserData(c.getId());
                rb.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151; -fx-cursor: hand;");
                rb.setWrapText(true);
                rb.selectedProperty().addListener((obs, o, n) -> {
                    if (n) {
                        selectedChoices.put(q.getId(), c.getId());
                        updateProgress();
                        card.setStyle("-fx-background-color: #fafbff; -fx-background-radius: 14; " +
                                "-fx-border-color: #6366f1; -fx-border-radius: 14; -fx-border-width: 2; " +
                                "-fx-effect: dropshadow(gaussian, rgba(99,102,241,0.12), 8, 0, 0, 2);");
                    }
                });
                choicesBox.getChildren().add(rb);
            }
            card.getChildren().add(choicesBox);
            questionsContainer.getChildren().add(card);
            idx++;
        }
    }

    private void updateProgress() {
        if (questions == null || questions.isEmpty()) return;
        double progress = (double) selectedChoices.size() / questions.size();
        quizProgressBar.setProgress(progress);
        progressInfoLbl.setText(selectedChoices.size() + " of " + questions.size() + " answered");
    }

    @FXML
    private void onSubmit() {
        // ✅ soumission normale: exige que tout soit répondu
        if (quiz == null || questions == null) return;
        User u = AuthService.getCurrentUser();
        if (u == null) return;

        if (selectedChoices.size() < questions.size()) {
            new Alert(Alert.AlertType.WARNING, "⚠ Please answer all questions before submitting.").showAndWait();
            return;
        }

        submitAndLock(true); // requireAllAnswered = true
    }

    // ✅ Soumission forcée (anti-cheat) : ne nécessite PAS toutes les réponses
    private void onSubmitForce() {
        if (quiz == null || questions == null) return;
        User u = AuthService.getCurrentUser();
        if (u == null) return;

        submitAndLock(false); // requireAllAnswered = false
    }

    private void submitAndLock(boolean requireAllAnswered) {
        if (quizSubmitted) return; // éviter double submit
        quizSubmitted = true;

        try {
            int score = 0;
            int total = questions.size();

            for (Question q : questions) {
                Integer chosenId = selectedChoices.get(q.getId());
                if (chosenId != null) {
                    List<Choix> choices = choixDAO.getByQuestionId(q.getId());
                    for (Choix c : choices) {
                        if (c.getId() == chosenId && c.isEstCorrect()) { score++; break; }
                    }
                }
            }

            User u = AuthService.getCurrentUser();

            TentativeQuiz t = new TentativeQuiz();
            t.setQuizId(quiz.getId());
            t.setUserId(u.getId());
            t.setScore(score);
            t.setTotal(total);
            tentativeDAO.add(t);

            submitBtn.setDisable(true);
            submitBtn.setText("✅ Submitted — " + score + "/" + total);
            submitBtn.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white; -fx-background-radius: 12; " +
                    "-fx-padding: 14 40; -fx-font-size: 15px; -fx-font-weight: 700;");

            double pct = total > 0 ? (100.0 * score / total) : 0;
            String emoji = pct >= 80 ? "🎉" : pct >= 60 ? "👍" : "📖";
            String msg = emoji + " You scored " + score + " out of " + total + " (" + (int)pct + "%)";
            if (pct >= 80) msg += "\nExcellent work!";
            else if (pct >= 60) msg += "\nGood job! Keep it up!";
            else msg += "\nKeep studying and try again!";

            // Highlight correct/wrong + disable
            for (Question q : questions) {
                ToggleGroup tg = answerGroups.get(q.getId());
                if (tg != null) {
                    List<Choix> choices = choixDAO.getByQuestionId(q.getId());
                    for (Toggle toggle : tg.getToggles()) {
                        RadioButton rb = (RadioButton) toggle;
                        int cId = (int) rb.getUserData();
                        for (Choix c : choices) {
                            if (c.getId() == cId) {
                                if (c.isEstCorrect()) {
                                    rb.setStyle("-fx-text-fill: #16a34a; -fx-font-weight: 700; -fx-font-size: 13px;");
                                } else if (rb.isSelected()) {
                                    rb.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: 700; -fx-font-size: 13px;");
                                }
                            }
                        }
                        rb.setDisable(true);
                    }
                }
            }

            quizProgressBar.setProgress(1.0);
            progressInfoLbl.setText("Quiz completed! Score: " + score + "/" + total);

            // message différent si anti-cheat
            if (focusLostCount >= MAX_FOCUS_LOST) {
                msg = "⚠ Anti-triche : quiz soumis automatiquement.\n\n" + msg;
            }

            new Alert(Alert.AlertType.INFORMATION, msg).showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        }
    }

    @FXML private void onBack() { SceneUtil.switchScene("Courses/CourseDetailCandidat.fxml"); }

    @FXML private void handleDashboard()     { SceneUtil.switchScene("dashboard.fxml"); }
    @FXML private void handleJobOffers()     { SceneUtil.switchScene("OffersCardView.fxml"); }
    @FXML private void handleTrends()        { SceneUtil.switchScene("MarketTrendsView.fxml"); }
    @FXML private void handleInterviews()    { SceneUtil.switchScene("Interviews/InterviewView.fxml"); }
    @FXML private 
    @javafx.fxml.FXML
    private void handleEvents() {
        talentospidev.models.User u = talentospidev.services.AuthService.getCurrentUser();
        boolean isRecruiter = u != null && (u.getRole() == talentospidev.models.User.Role.HR || u.getRole() == talentospidev.models.User.Role.ADMIN);
        talentospidev.utils.SceneUtil.switchScene(isRecruiter ? "Events/EventsFeed.fxml" : "Events/EventsBrowse.fxml");
    }
    void handleCourses()       { SceneUtil.switchScene("Courses/CoursesBrowse.fxml"); }
    @FXML private void handleMyCircle()      { SceneUtil.switchScene("my_circle.fxml"); }
    @FXML private void handleNotifications() { SceneUtil.switchScene("notifications.fxml"); }
    @FXML private void handleToDo()          { SceneUtil.switchScene("todo.fxml"); }
    @FXML private void handleActivities()    { SceneUtil.switchScene("activities/activities.fxml"); }
    @FXML private void handleProjects()      { SceneUtil.switchScene("projects/projects.fxml"); }
    @FXML private void handleMyProfile()     { SceneUtil.switchScene("profile-view.fxml"); }
    @FXML private void handleSettings()      { SceneUtil.switchScene("settings.fxml"); }
    @FXML private void handleLogout()        { AuthService.logout(); SceneUtil.switchScene("login.fxml"); }
}