package talentospidev.controllers.courses;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import talentospidev.dao.coursesDAO.ChoixDAO;
import talentospidev.dao.coursesDAO.QuestionDAO;
import talentospidev.models.courses.Choix;
import talentospidev.models.courses.Question;
import talentospidev.services.AuthService;
import talentospidev.utils.SceneUtil;
import talentospidev.utils.ViewContext;

public class QuestionFormController {

    @FXML private Label errorLbl;
    @FXML private TextField enonceField, choice1Field, choice2Field, choice3Field, choice4Field;
    @FXML private VBox sidebar;
    @FXML private Button toDoTab, activitiesTab, projectsTab;

    private final QuestionDAO questionDAO = new QuestionDAO();
    private final ChoixDAO choixDAO = new ChoixDAO();

    @FXML
    public void initialize() {
        talentospidev.utils.SidebarUtil.configure(toDoTab, activitiesTab, projectsTab);
    }

    @FXML
    private void onSave() {
        StringBuilder err = new StringBuilder();
        if (enonceField.getText().trim().isEmpty()) err.append("Question text required. ");
        if (choice1Field.getText().trim().isEmpty()) err.append("Correct answer required. ");

        if (!err.isEmpty()) {
            errorLbl.setText("⚠ " + err.toString().trim());
            errorLbl.setVisible(true); errorLbl.setManaged(true);
            return;
        }

        try {
            Question q = new Question();
            q.setQuizId(ViewContext.getSelectedQuizId());
            q.setEnonce(enonceField.getText().trim());
            q.setPoints(1); q.setOrdre(1); q.setPublished(true);
            questionDAO.add(q);

            String[] texts = { choice1Field.getText(), choice2Field.getText(), choice3Field.getText(), choice4Field.getText() };
            for (int i = 0; i < texts.length; i++) {
                if (!texts[i].trim().isEmpty()) {
                    Choix ch = new Choix();
                    ch.setQuestionId(q.getId());
                    ch.setTexte(texts[i].trim());
                    ch.setEstCorrect(i == 0);
                    choixDAO.add(ch);
                }
            }
            SceneUtil.switchScene("Courses/QuizRH.fxml");
        } catch (Exception ex) {
            ex.printStackTrace();
            errorLbl.setText("⚠ " + ex.getMessage());
            errorLbl.setVisible(true); errorLbl.setManaged(true);
        }
    }

    @FXML private void onCancel() { SceneUtil.switchScene("Courses/QuizRH.fxml"); }

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
    void handleCourses()       { SceneUtil.switchScene("Courses/CoursesRH.fxml"); }
    @FXML private void handleMyCircle()      { SceneUtil.switchScene("my_circle.fxml"); }
    @FXML private void handleNotifications() { SceneUtil.switchScene("notifications.fxml"); }
    @FXML private void handleToDo()          { SceneUtil.switchScene("todo.fxml"); }
    @FXML private void handleActivities()    { SceneUtil.switchScene("activities/activities.fxml"); }
    @FXML private void handleProjects()      { SceneUtil.switchScene("projects/projects.fxml"); }
    @FXML private void handleMyProfile()     { SceneUtil.switchScene("profile-view.fxml"); }
    @FXML private void handleSettings()      { SceneUtil.switchScene("settings.fxml"); }
    @FXML private void handleLogout()        { AuthService.logout(); SceneUtil.switchScene("login.fxml"); }
}
