package talentospidev.controllers.events;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import talentospidev.dao.UserDao;
import talentospidev.dao.eventsDAO.*;
import talentospidev.models.User;
import talentospidev.models.events.*;
import talentospidev.services.AuthService;
import talentospidev.services.EventShareService;
import talentospidev.utils.SceneUtil;
import talentospidev.utils.ViewContext;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class EventDetailController {

    @FXML private Label titleLbl, metaLbl, descLbl, locationLbl, dateLbl, capacityLbl, organizerLbl;
    @FXML private VBox commentsContainer, organizerCard, infoCard, sidebar;
    @FXML private HBox actionsRow;
    @FXML private TextField commentField;
    @FXML private Button toDoTab, activitiesTab, projectsTab;

    private final EventDAO eventDAO = new EventDAO();
    private final EventLikeDAO likeDAO = new EventLikeDAO();
    private final EventCommentDAO commentDAO = new EventCommentDAO();
    private final EventParticipationDAO participationDAO = new EventParticipationDAO();
    private static final DateTimeFormatter dtFmt = DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy 'at' HH:mm");
    private Event event;

    @FXML
    public void initialize() {
        talentospidev.utils.SidebarUtil.configure(toDoTab, activitiesTab, projectsTab);
        int evId = ViewContext.getSelectedEventId();
        if (evId > 0) {
            try {
                event = eventDAO.getById(evId);
                if (event != null) loadEventDetails();
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void loadEventDetails() throws Exception {
        User u = AuthService.getCurrentUser();

        titleLbl.setText("📅 " + event.getTitle());
        metaLbl.setText(safe(event.getEventType()) + "  •  " + safe(event.getStatus()));
        descLbl.setText(event.getDescription() != null ? event.getDescription() : "No description.");
        locationLbl.setText(event.isOnline() ? "🌐 Online — " + safe(event.getOnlineLink()) : "📍 " + safe(event.getLocation()));
        dateLbl.setText("📅 " + (event.getEventDate() != null ? dtFmt.format(event.getEventDate()) : "TBD") +
                (event.getEndDate() != null ? " → " + dtFmt.format(event.getEndDate()) : ""));

        int attendees = participationDAO.countByEventId(event.getId());
        capacityLbl.setText("👥 " + attendees + " attendees" + (event.getMaxCapacity() > 0 ? " / " + event.getMaxCapacity() + " max" : ""));

        // Organizer
        try {
            User org = new UserDao().findById(event.getOrganizerId());
            organizerLbl.setText(org != null ? org.getEmail() : "Unknown");
        } catch (Exception e) { organizerLbl.setText("Organizer #" + event.getOrganizerId()); }

        // Actions
        actionsRow.getChildren().clear();
        int likes = likeDAO.countByEventId(event.getId());
        boolean liked = u != null && likeDAO.hasLiked(event.getId(), u.getId());

        Button likeBtn = new Button((liked ? "❤️ " : "🤍 ") + likes + " Likes");
        likeBtn.setStyle("-fx-background-color: " + (liked ? "#fee2e2" : "#f3f4f6") + "; " +
                "-fx-text-fill: " + (liked ? "#dc2626" : "#374151") + "; -fx-background-radius: 10; " +
                "-fx-padding: 10 20; -fx-font-weight: 700; -fx-cursor: hand; -fx-font-size: 13px;");
        likeBtn.setOnAction(e -> {
            try { if (u != null) { likeDAO.toggle(event.getId(), u.getId()); loadEventDetails(); } } catch (Exception ex) { ex.printStackTrace(); }
        });

        actionsRow.getChildren().add(likeBtn);

        // Attend button for candidates
        if (u != null && u.getRole() == User.Role.CANDIDATE) {
            String status = participationDAO.getStatus(event.getId(), u.getId());
            if (status == null) {
                Button attendBtn = new Button("🎟 Attend Event");
                attendBtn.setStyle("-fx-background-color: linear-gradient(to right, #22c55e, #16a34a); -fx-text-fill: white; " +
                        "-fx-background-radius: 10; -fx-padding: 10 24; -fx-font-weight: 700; -fx-cursor: hand; -fx-font-size: 13px;");
                attendBtn.setOnAction(e -> { ViewContext.setSelectedEventId(event.getId()); SceneUtil.switchScene("Events/AttendanceConfirm.fxml"); });
                actionsRow.getChildren().add(attendBtn);
            } else {
                Label statusLbl = new Label("✅ " + status);
                statusLbl.setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #166534; -fx-background-radius: 10; " +
                        "-fx-padding: 10 20; -fx-font-weight: 700; -fx-font-size: 13px;");
                actionsRow.getChildren().add(statusLbl);
            }
        }

        // Stats button
        Button statsBtn = new Button("📊 Stats");
        statsBtn.setStyle("-fx-background-color: #eef2ff; -fx-text-fill: #6366f1; -fx-background-radius: 10; " +
                "-fx-padding: 10 20; -fx-font-weight: 700; -fx-cursor: hand; -fx-font-size: 13px;");
        statsBtn.setOnAction(e -> { ViewContext.setSelectedEventId(event.getId()); SceneUtil.switchScene("Events/EventStats.fxml"); });
        actionsRow.getChildren().add(statsBtn);

        // Load comments
        loadComments();
    }

    private void loadComments() throws Exception {
        commentsContainer.getChildren().clear();
        List<EventComment> comments = commentDAO.getByEventId(event.getId());
        for (EventComment c : comments) {
            HBox row = new HBox(10);
            row.setAlignment(Pos.TOP_LEFT);
            row.setPadding(new Insets(8, 12, 8, 12));
            row.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 10;");

            Label avatar = new Label("👤");
            avatar.setStyle("-fx-font-size: 16px;");

            VBox content = new VBox(2);
            HBox.setHgrow(content, Priority.ALWAYS);

            String userName;
            try { User cu = new UserDao().findById(c.getUserId()); userName = cu != null ? cu.getEmail() : "User"; } catch (Exception e) { userName = "User"; }
            Label nameLbl = new Label(userName);
            nameLbl.setStyle("-fx-font-size: 11px; -fx-font-weight: 700; -fx-text-fill: #6366f1;");
            Label textLbl = new Label(c.getContent());
            textLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #374151;");
            textLbl.setWrapText(true);

            String timeAgo = c.getCreatedAt() != null ? DateTimeFormatter.ofPattern("dd MMM HH:mm").format(c.getCreatedAt()) : "";
            Label timeLbl = new Label(timeAgo);
            timeLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #d1d5db;");

            content.getChildren().addAll(nameLbl, textLbl, timeLbl);
            row.getChildren().addAll(avatar, content);
            commentsContainer.getChildren().add(row);
        }
    }

    @FXML
    private void onAddComment() {
        if (event == null || commentField.getText().trim().isEmpty()) return;
        User u = AuthService.getCurrentUser();
        if (u == null) return;
        try {
            EventComment c = new EventComment();
            c.setEventId(event.getId());
            c.setUserId(u.getId());
            c.setContent(commentField.getText().trim());
            commentDAO.add(c);
            commentField.clear();
            loadComments();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void shareOnFacebook() { if (event != null) EventShareService.shareOnFacebook(event.getTitle(), EventShareService.buildEventUrl(event.getId())); }
    @FXML private void shareOnTwitter()  { if (event != null) EventShareService.shareOnTwitter(event.getTitle(), EventShareService.buildEventUrl(event.getId())); }
    @FXML private void shareOnLinkedIn() { if (event != null) EventShareService.shareOnLinkedIn(event.getTitle(), EventShareService.buildEventUrl(event.getId())); }

    @FXML private void onBack() { SceneUtil.switchScene("Events/EventsFeed.fxml"); }
    private String safe(String s) { return s == null ? "" : s; }

    @FXML private void handleDashboard()     { SceneUtil.switchScene("dashboard.fxml"); }
    @FXML private void handleJobOffers()     { SceneUtil.switchScene("OffersCardView.fxml"); }
    @FXML private void handleTrends()        { SceneUtil.switchScene("MarketTrendsView.fxml"); }
    @FXML private void handleInterviews()    { SceneUtil.switchScene("Interviews/InterviewView.fxml"); }
    @FXML private void handleCourses()       { SceneUtil.switchScene("Courses/CoursesRH.fxml"); }
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
