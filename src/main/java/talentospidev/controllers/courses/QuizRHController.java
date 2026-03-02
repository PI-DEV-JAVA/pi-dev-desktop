package talentospidev.controllers.courses;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import talentospidev.dao.coursesDAO.ChoixDAO;
import talentospidev.dao.coursesDAO.QuestionDAO;
import talentospidev.dao.coursesDAO.QuizDAO;
import talentospidev.models.courses.Choix;
import talentospidev.models.courses.Question;
import talentospidev.models.courses.Quiz;
import talentospidev.services.AuthService;
import talentospidev.services.AutoQuizGeneratorService;
import talentospidev.utils.SceneUtil;
import talentospidev.utils.ViewContext;

import java.util.List;

public class QuizRHController {

    @FXML private Label quizTitleLbl, quizDescLbl;
    @FXML private VBox questionsContainer, sidebar;
    @FXML private Button toDoTab, activitiesTab, projectsTab;

    private final QuizDAO quizDAO = new QuizDAO();
    private final QuestionDAO questionDAO = new QuestionDAO();
    private final ChoixDAO choixDAO = new ChoixDAO();
    private Quiz quiz;

    @FXML
    public void initialize() {
        talentospidev.utils.SidebarUtil.configure(toDoTab, activitiesTab, projectsTab);
        int qId = ViewContext.getSelectedQuizId();
        if (qId > 0) {
            try {
                quiz = quizDAO.getById(qId);
                if (quiz != null) {
                    quizTitleLbl.setText("📝 " + quiz.getTitre());
                    quizDescLbl.setText(quiz.getDescription() != null ? quiz.getDescription() : "");
                    loadQuestions();
                }
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void loadQuestions() throws Exception {
        questionsContainer.getChildren().clear();
        List<Question> questions = questionDAO.getByQuizId(quiz.getId());

        if (questions.isEmpty()) {
            VBox emptyBox = new VBox(8);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(40));
            emptyBox.setStyle("-fx-background-color: white; -fx-background-radius: 14; -fx-border-color: #e5e7eb; -fx-border-radius: 14;");
            Label emptyLbl = new Label("No questions yet");
            emptyLbl.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; -fx-text-fill: #9ca3af;");
            Label hintLbl = new Label("Add manually or use AI auto-generation ✨");
            hintLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #d1d5db;");
            emptyBox.getChildren().addAll(emptyLbl, hintLbl);
            questionsContainer.getChildren().add(emptyBox);
            return;
        }

        int idx = 1;
        for (Question q : questions) {
            VBox card = new VBox(8);
            card.setPadding(new Insets(16, 20, 16, 20));
            card.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                    "-fx-border-color: #e5e7eb; -fx-border-radius: 12; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.03), 4, 0, 0, 1);");

            HBox qHeader = new HBox(8);
            qHeader.setAlignment(Pos.CENTER_LEFT);
            Label numLbl = new Label("Q" + idx);
            numLbl.setStyle("-fx-background-color: #6366f1; -fx-text-fill: white; -fx-background-radius: 6; " +
                    "-fx-padding: 2 8; -fx-font-size: 11px; -fx-font-weight: 700;");
            Label qLbl = new Label(q.getEnonce());
            qLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: 700; -fx-text-fill: #111827;");
            qLbl.setWrapText(true);
            HBox.setHgrow(qLbl, Priority.ALWAYS);

            Button delBtn = new Button("🗑");
            delBtn.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-background-radius: 6; " +
                    "-fx-padding: 3 8; -fx-cursor: hand; -fx-font-size: 11px;");
            delBtn.setOnAction(e -> { try { questionDAO.delete(q.getId()); loadQuestions(); } catch (Exception ex) { ex.printStackTrace(); } });

            qHeader.getChildren().addAll(numLbl, qLbl, delBtn);
            card.getChildren().add(qHeader);

            List<Choix> choices = choixDAO.getByQuestionId(q.getId());
            for (Choix c : choices) {
                Label cLbl = new Label((c.isEstCorrect() ? "✅ " : "⬜ ") + c.getTexte());
                cLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: " + (c.isEstCorrect() ? "#16a34a; -fx-font-weight: 700" : "#6b7280") + ";");
                cLbl.setWrapText(true);
                cLbl.setPadding(new Insets(0, 0, 0, 28));
                card.getChildren().add(cLbl);
            }

            questionsContainer.getChildren().add(card);
            idx++;
        }
    }

    @FXML
    private void onAddQuestion() {
        // Navigate to separate question form page
        SceneUtil.switchScene("Courses/QuestionForm.fxml");
    }

    @FXML
    private void onAutoGenerate() {
        if (quiz == null) return;
        try {
            new AutoQuizGeneratorService().generateIfEmpty(quiz);
            loadQuestions();
            new Alert(Alert.AlertType.INFORMATION, "🤖 AI questions generated successfully!").showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Generation failed: " + e.getMessage()).showAndWait();
        }
    }

    @FXML private void onBack() { SceneUtil.switchScene("Courses/CourseDetails.fxml"); }

    @FXML private void handleDashboard()     { SceneUtil.switchScene("dashboard.fxml"); }
    @FXML private void handleJobOffers()     { SceneUtil.switchScene("OffersCardView.fxml"); }
    @FXML private void handleTrends()        { SceneUtil.switchScene("MarketTrendsView.fxml"); }
    @FXML private void handleInterviews()    { SceneUtil.switchScene("Interviews/InterviewView.fxml"); }
    @FXML private void handleCourses()       { SceneUtil.switchScene("Courses/CoursesRH.fxml"); }
    @FXML private void handleMyCircle()      { SceneUtil.switchScene("my_circle.fxml"); }
    @FXML private void handleNotifications() { SceneUtil.switchScene("notifications.fxml"); }
    @FXML private void handleToDo()          { SceneUtil.switchScene("todo.fxml"); }
    @FXML private void handleActivities()    { SceneUtil.switchScene("activities/activities.fxml"); }
    @FXML private void handleProjects()      { SceneUtil.switchScene("projects/projects.fxml"); }
    @FXML private void handleMyProfile()     { SceneUtil.switchScene("profile-view.fxml"); }
    @FXML private void handleSettings()      { SceneUtil.switchScene("settings.fxml"); }
    @FXML private void handleLogout()        { AuthService.logout(); SceneUtil.switchScene("login.fxml"); }
}
