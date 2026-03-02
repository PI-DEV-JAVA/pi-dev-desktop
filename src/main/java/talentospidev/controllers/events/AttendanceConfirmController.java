package talentospidev.controllers.events;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import talentospidev.dao.eventsDAO.*;
import talentospidev.models.User;
import talentospidev.models.events.*;
import talentospidev.services.AuthService;
import talentospidev.services.QRCodeService;
import talentospidev.utils.SceneUtil;
import talentospidev.utils.ViewContext;

import java.time.format.DateTimeFormatter;

public class AttendanceConfirmController {

    @FXML private Label eventTitleLbl, eventDateLbl, statusLbl, qrHintLbl;
    @FXML private Button confirmBtn, generateQrBtn;
    @FXML private ImageView qrImageView;
    @FXML private VBox sidebar;
    @FXML private Button toDoTab, activitiesTab, projectsTab;

    private final EventDAO eventDAO = new EventDAO();
    private final EventParticipationDAO participationDAO = new EventParticipationDAO();
    private Event event;
    private static final DateTimeFormatter dtFmt = DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy 'at' HH:mm");

    @FXML
    public void initialize() {
        talentospidev.utils.SidebarUtil.configure(toDoTab, activitiesTab, projectsTab);
        int evId = ViewContext.getSelectedEventId();
        if (evId > 0) {
            try {
                event = eventDAO.getById(evId);
                if (event != null) {
                    eventTitleLbl.setText(event.getTitle());
                    eventDateLbl.setText("📅 " + (event.getEventDate() != null ? dtFmt.format(event.getEventDate()) : "TBD"));
                    loadStatus();
                }
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void loadStatus() throws Exception {
        User u = AuthService.getCurrentUser();
        if (u == null || event == null) return;

        String status = participationDAO.getStatus(event.getId(), u.getId());
        if (status != null) {
            statusLbl.setText("✅ Status: " + status);
            statusLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: 700; -fx-text-fill: #16a34a;");
            confirmBtn.setDisable(true);
            confirmBtn.setText("Already Registered");
            confirmBtn.setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #166534; -fx-background-radius: 12; " +
                    "-fx-padding: 14 36; -fx-font-size: 15px; -fx-font-weight: 700;");
        } else {
            statusLbl.setText("You haven't registered yet");
            statusLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: 700; -fx-text-fill: #d97706;");
        }
    }

    @FXML
    private void onConfirm() {
        User u = AuthService.getCurrentUser();
        if (u == null || event == null) return;
        try {
            EventParticipation p = new EventParticipation();
            p.setEventId(event.getId());
            p.setUserId(u.getId());
            p.setStatus("CONFIRMED");
            participationDAO.add(p);
            loadStatus();
            new Alert(Alert.AlertType.INFORMATION, "🎉 You're registered for " + event.getTitle() + "!").showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Registration failed: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void onGenerateQR() {
        User u = AuthService.getCurrentUser();
        if (u == null || event == null) return;
        String data = QRCodeService.buildEventQRData(event.getId(), u.getId());
        javafx.scene.image.Image qr = QRCodeService.generateQRCode(data, 300, 300);
        if (qr != null) {
            qrImageView.setImage(qr);
            qrHintLbl.setText("📱 Scan this QR to view event statistics");
            // Save QR reference
            try { participationDAO.updateQrCode(event.getId(), u.getId(), data); } catch (Exception e) { e.printStackTrace(); }
        }
    }

    @FXML private void onBack() { ViewContext.setSelectedEventId(event != null ? event.getId() : -1); SceneUtil.switchScene("Events/EventDetail.fxml"); }

    @FXML private void handleDashboard()     { SceneUtil.switchScene("dashboard.fxml"); }
    @FXML private void handleJobOffers()     { SceneUtil.switchScene("OffersCardView.fxml"); }
    @FXML private void handleTrends()        { SceneUtil.switchScene("MarketTrendsView.fxml"); }
    @FXML private void handleInterviews()    { SceneUtil.switchScene("Interviews/InterviewView.fxml"); }
    @FXML private void handleCourses()       { SceneUtil.switchScene("Courses/CoursesBrowse.fxml"); }
    @FXML private void handleEvents()        { SceneUtil.switchScene("Events/EventsFeed.fxml"); }
    @FXML private void handleMyCircle()      { SceneUtil.switchScene("my_circle.fxml"); }
    @FXML private void handleNotifications() { SceneUtil.switchScene("notifications.fxml"); }
    @FXML private void handleToDo()          { SceneUtil.switchScene("todo.fxml"); }
    @FXML private void handleActivities()    { SceneUtil.switchScene("activities/activities.fxml"); }
    @FXML private void handleProjects()      { SceneUtil.switchScene("projects/projects.fxml"); }
    @FXML private void handleMyProfile()     { SceneUtil.switchScene("profile-view.fxml"); }
    @FXML private void handleSettings()      { SceneUtil.switchScene("settings.fxml"); }
    @FXML private void handleLogout()        { AuthService.logout(); SceneUtil.switchScene("login.fxml"); }
}
