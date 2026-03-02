package talentospidev.controllers.courses;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import talentospidev.dao.coursesDAO.QuizDAO;
import talentospidev.models.courses.Quiz;
import talentospidev.services.AuthService;
import talentospidev.utils.SceneUtil;
import talentospidev.utils.ViewContext;

public class QuizFormController {

    @FXML private Label errorLbl;
    @FXML private TextField titreField, dureeField;
    @FXML private TextArea descField;
    @FXML private VBox sidebar;
    @FXML private Button toDoTab, activitiesTab, projectsTab;

    private final QuizDAO quizDAO = new QuizDAO();

    @FXML
    public void initialize() {
        talentospidev.utils.SidebarUtil.configure(toDoTab, activitiesTab, projectsTab);
    }

    @FXML
    private void onSave() {
        StringBuilder err = new StringBuilder();
        if (titreField.getText().trim().isEmpty()) err.append("Title required. ");
        try { Integer.parseInt(dureeField.getText().trim()); } catch (Exception e) { err.append("Duration must be a number. "); }

        if (!err.isEmpty()) {
            errorLbl.setText("⚠ " + err.toString().trim());
            errorLbl.setVisible(true); errorLbl.setManaged(true);
            return;
        }

        try {
            Quiz q = new Quiz();
            q.setTitre(titreField.getText().trim());
            q.setDescription(descField.getText());
            q.setDureeMinutes(Integer.parseInt(dureeField.getText().trim()));
            q.setActif(true);
            q.setSeanceId(ViewContext.getSelectedSeanceId());
            quizDAO.add(q);
            SceneUtil.switchScene("Courses/CourseDetails.fxml");
        } catch (Exception ex) {
            ex.printStackTrace();
            errorLbl.setText("⚠ " + ex.getMessage());
            errorLbl.setVisible(true); errorLbl.setManaged(true);
        }
    }

    @FXML private void onCancel() { SceneUtil.switchScene("Courses/CourseDetails.fxml"); }

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
