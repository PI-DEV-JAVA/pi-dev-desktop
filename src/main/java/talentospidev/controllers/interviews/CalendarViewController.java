package talentospidev.controllers.interviews;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import talentospidev.dao.interviewsDAO.InterviewMeetDAO;
import talentospidev.models.User;
import talentospidev.models.interviews.InterviewMeet;
import talentospidev.services.AuthService;
import talentospidev.utils.SceneUtil;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.List;

public class CalendarViewController {
    @FXML
    private StackPane calendarContainer;
    @FXML
    private Button toDoTab;
    @FXML
    private Button activitiesTab;
    @FXML
    private Button projectsTab;
    @FXML
    private Button backBtn;

    private CalendarView calendarView;
    private Calendar interviewCalendar;
    private InterviewMeetDAO meetDAO;

    public void initialize() {
        try {
            talentospidev.utils.SidebarUtil.configure(toDoTab, activitiesTab, projectsTab);
            meetDAO = new InterviewMeetDAO();

            // candidate can't go back to interview list
            User u = AuthService.getCurrentUser();
            boolean isRecruiter = u != null && (u.getRole() == User.Role.HR || u.getRole() == User.Role.ADMIN);
            if (backBtn != null) {
                backBtn.setVisible(isRecruiter);
                backBtn.setManaged(isRecruiter);
            }

            setupCalendarView();
            loadMeetings();
            startClockThread();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupCalendarView() {
        calendarView = new CalendarView();
        interviewCalendar = new Calendar("Interviews");

        CalendarSource mySource = new CalendarSource("My Sources");
        mySource.getCalendars().add(interviewCalendar);
        calendarView.getCalendarSources().setAll(mySource);

        interviewCalendar.setReadOnly(false);
        calendarView.showMonthPage();
        calendarView.setShowPrintButton(false);
        calendarView.setShowPageSwitcher(false);
        calendarView.setShowSourceTrayButton(false);
        calendarView.setShowSearchField(false);

        calendarContainer.getChildren().add(calendarView);

        calendarView.setEntryDetailsCallback(param -> {
            Entry<?> entry = param.getEntry();
            InterviewMeet meet = (InterviewMeet) entry.getUserObject();
            if (meet != null)
                joinMeet(meet);
            return null;
        });
    }

    public void loadMeetings() {
        User u = AuthService.getCurrentUser();
        int userId = u != null ? u.getId() : 1;
        List<InterviewMeet> meets = meetDAO.findByUserId(userId);

        Platform.runLater(() -> {
            interviewCalendar.clear();
            if (meets != null) {
                for (InterviewMeet meet : meets) {
                    Entry<InterviewMeet> entry = new Entry<>("Meet: " + meet.getUuid());
                    entry.setInterval(meet.getScheduledAt(), meet.getScheduledAt().plusHours(1));
                    entry.setUserObject(meet);
                    interviewCalendar.addEntry(entry);
                }
            }
        });
    }

    private void startClockThread() {
        Thread thread = new Thread(() -> {
            while (true) {
                Platform.runLater(() -> calendarView.setTime(LocalTime.now()));
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public void joinMeet(InterviewMeet meet) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Interviews/videoPreview.fxml"));
            Parent root = loader.load();
            MediaController controller = loader.getController();
            controller.initData(meet);
            Stage stage = new Stage();
            stage.setTitle("Interview Meeting - " + meet.getUuid());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Could not open the meeting window: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onBack() {
        SceneUtil.switchScene("Interviews/InterviewView.fxml");
    }

    // ===== Sidebar Navigation =====
    @FXML
    private void handleDashboard() {
        User u = AuthService.getCurrentUser();
        if (u != null && u.getRole() == User.Role.HR)
            SceneUtil.switchScene("recruiter_dashboard.fxml");
        else if (u != null && u.getRole() == User.Role.ADMIN)
            SceneUtil.switchScene("admin_dashboard.fxml");
        else
            SceneUtil.switchScene("dashboard.fxml");
    }

    @FXML
    private void handleMyProfile() {
        SceneUtil.switchScene("profile-view.fxml");
    }

    @FXML
    private void handleJobOffers() {
        SceneUtil.switchScene("OffersCardView.fxml");
    }

    @FXML
    private void handleTrends() {
        SceneUtil.switchScene("MarketTrendsView.fxml");
    }

    @FXML
    private void handleInterviews() {
        SceneUtil.switchScene("Interviews/InterviewView.fxml");
    }

    @FXML
    private void handleToDo() {
        SceneUtil.switchScene("activities/activity_employee.fxml");
    }

    @FXML
    private void handleActivities() {
        SceneUtil.switchScene("activities/activities.fxml");
    }

    @FXML
    private void handleProjects() {
        SceneUtil.switchScene("projects/projects.fxml");
    }

    @FXML
    private void handleMyCircle() {
        SceneUtil.switchScene("my_circle.fxml");
    }

    @FXML
    private void handleNotifications() {
        SceneUtil.switchScene("notifications.fxml");
    }

    @FXML
    private void handleSettings() {
        SceneUtil.switchScene("settings.fxml");
    }

    @FXML
    private void handleLogout() {
        AuthService.logout();
        SceneUtil.switchScene("login.fxml");
    }
}