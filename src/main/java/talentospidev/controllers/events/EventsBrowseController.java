package talentospidev.controllers.events;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import talentospidev.dao.eventsDAO.*;
import talentospidev.models.User;
import talentospidev.models.events.*;
import talentospidev.services.AuthService;
import talentospidev.utils.SceneUtil;
import talentospidev.utils.ViewContext;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Candidate-facing event browse page — upcoming events + my registered events.
 */
public class EventsBrowseController {

    @FXML private VBox eventsContainer, sidebar;
    @FXML private TextField searchField;
    @FXML private Button upcomingTab, myEventsTab;
    @FXML private Button toDoTab, activitiesTab, projectsTab;

    private final EventDAO eventDAO = new EventDAO();
    private final EventLikeDAO likeDAO = new EventLikeDAO();
    private final EventCommentDAO commentDAO = new EventCommentDAO();
    private final EventParticipationDAO participationDAO = new EventParticipationDAO();
    private static final DateTimeFormatter dtFmt = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    private boolean showingUpcoming = true;

    @FXML
    public void initialize() {
        talentospidev.utils.SidebarUtil.configure(toDoTab, activitiesTab, projectsTab);
        loadUpcoming();
    }

    @FXML private void showUpcoming() {
        showingUpcoming = true;
        upcomingTab.setStyle("-fx-background-color: #6366f1; -fx-text-fill: white; -fx-background-radius: 10 0 0 10; -fx-padding: 10 24; -fx-font-weight: 700; -fx-cursor: hand;");
        myEventsTab.setStyle("-fx-background-color: #f3f4f6; -fx-text-fill: #374151; -fx-background-radius: 0 10 10 0; -fx-padding: 10 24; -fx-font-weight: 700; -fx-cursor: hand;");
        loadUpcoming();
    }

    @FXML private void showMyEvents() {
        showingUpcoming = false;
        myEventsTab.setStyle("-fx-background-color: #6366f1; -fx-text-fill: white; -fx-background-radius: 0 10 10 0; -fx-padding: 10 24; -fx-font-weight: 700; -fx-cursor: hand;");
        upcomingTab.setStyle("-fx-background-color: #f3f4f6; -fx-text-fill: #374151; -fx-background-radius: 10 0 0 10; -fx-padding: 10 24; -fx-font-weight: 700; -fx-cursor: hand;");
        loadMyEvents();
    }

    private void loadUpcoming() {
        eventsContainer.getChildren().clear();
        try {
            User u = AuthService.getCurrentUser();
            List<Event> events = eventDAO.getUpcoming();
            String q = searchField.getText() == null ? "" : searchField.getText().toLowerCase();
            for (Event ev : events) {
                if (!q.isEmpty() && (ev.getTitle() == null || !ev.getTitle().toLowerCase().contains(q))) continue;
                eventsContainer.getChildren().add(buildCard(ev, u));
            }
            if (eventsContainer.getChildren().isEmpty()) {
                Label empty = new Label("No upcoming events at the moment 🌙");
                empty.setStyle("-fx-font-size: 15px; -fx-text-fill: #9ca3af; -fx-padding: 30;");
                eventsContainer.getChildren().add(empty);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadMyEvents() {
        eventsContainer.getChildren().clear();
        try {
            User u = AuthService.getCurrentUser();
            if (u == null) return;
            List<EventParticipation> participations = participationDAO.getByUserId(u.getId());
            for (EventParticipation p : participations) {
                Event ev = eventDAO.getById(p.getEventId());
                if (ev != null) eventsContainer.getChildren().add(buildCard(ev, u));
            }
            if (eventsContainer.getChildren().isEmpty()) {
                Label empty = new Label("You haven't joined any events yet 🎟");
                empty.setStyle("-fx-font-size: 15px; -fx-text-fill: #9ca3af; -fx-padding: 30;");
                eventsContainer.getChildren().add(empty);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private VBox buildCard(Event ev, User u) throws Exception {
        VBox card = new VBox(0);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-border-color: #e5e7eb; -fx-border-radius: 16; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 3);");

        // Header
        String[] gradients = {"#6366f1,#8b5cf6", "#ec4899,#f43f5e", "#14b8a6,#06b6d4", "#f59e0b,#ef4444"};
        String grad = gradients[Math.abs(ev.getTitle().hashCode()) % gradients.length];
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: linear-gradient(to right, " + grad + "); -fx-background-radius: 16 16 0 0; -fx-padding: 18 22;");

        VBox headerInfo = new VBox(4); HBox.setHgrow(headerInfo, Priority.ALWAYS);
        Label typeBadge = new Label(ev.getEventType() != null ? ev.getEventType() : "EVENT");
        typeBadge.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 2 8; -fx-font-size: 10px; -fx-font-weight: 700;");
        Label titleLbl = new Label(ev.getTitle());
        titleLbl.setStyle("-fx-font-size: 17px; -fx-font-weight: 800; -fx-text-fill: white;"); titleLbl.setWrapText(true);
        Label dateLbl = new Label("📅 " + (ev.getEventDate() != null ? dtFmt.format(ev.getEventDate()) : "TBD"));
        dateLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(255,255,255,0.85);");
        headerInfo.getChildren().addAll(typeBadge, titleLbl, dateLbl);
        header.getChildren().add(headerInfo);

        // Body
        VBox body = new VBox(8); body.setPadding(new Insets(14, 22, 14, 22));

        String locText = ev.isOnline() ? "🌐 Online" : ("📍 " + (ev.getLocation() != null ? ev.getLocation() : "TBD"));
        Label locLbl = new Label(locText); locLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af;");

        // Stats
        int likes = likeDAO.countByEventId(ev.getId());
        int comments = commentDAO.countByEventId(ev.getId());
        int attendees = participationDAO.countByEventId(ev.getId());
        boolean liked = u != null && likeDAO.hasLiked(ev.getId(), u.getId());

        HBox stats = new HBox(12); stats.setAlignment(Pos.CENTER_LEFT);
        Button likeBtn = new Button((liked ? "❤️ " : "🤍 ") + likes);
        likeBtn.setStyle("-fx-background-color: " + (liked ? "#fee2e2" : "#f3f4f6") + "; -fx-text-fill: " + (liked ? "#dc2626" : "#6b7280") + "; -fx-background-radius: 8; -fx-padding: 5 10; -fx-font-size: 11px; -fx-font-weight: 700; -fx-cursor: hand;");
        likeBtn.setOnAction(e -> { try { if (u != null) { likeDAO.toggle(ev.getId(), u.getId()); if (showingUpcoming) loadUpcoming(); else loadMyEvents(); } } catch (Exception ex) { ex.printStackTrace(); } });

        Label commentLbl = new Label("💬 " + comments); commentLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #6b7280; -fx-font-weight: 600;");
        Label attendeeLbl = new Label("👥 " + attendees); attendeeLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #6b7280; -fx-font-weight: 600;");

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

        // Attend / View button
        String participationStatus = u != null ? participationDAO.getStatus(ev.getId(), u.getId()) : null;
        Button actionBtn;
        if (participationStatus != null) {
            actionBtn = new Button("✅ " + participationStatus);
            actionBtn.setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #166534; -fx-background-radius: 8; -fx-padding: 6 14; -fx-font-size: 11px; -fx-font-weight: 700; -fx-cursor: hand;");
            actionBtn.setOnAction(e -> { ViewContext.setSelectedEventId(ev.getId()); SceneUtil.switchScene("Events/EventDetail.fxml"); });
        } else {
            actionBtn = new Button("🎟 Attend");
            actionBtn.setStyle("-fx-background-color: linear-gradient(to right, #22c55e, #16a34a); -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 6 14; -fx-font-size: 11px; -fx-font-weight: 700; -fx-cursor: hand;");
            actionBtn.setOnAction(e -> { ViewContext.setSelectedEventId(ev.getId()); SceneUtil.switchScene("Events/AttendanceConfirm.fxml"); });
        }

        Button detailBtn = new Button("→");
        detailBtn.setStyle("-fx-background-color: #eef2ff; -fx-text-fill: #6366f1; -fx-background-radius: 8; -fx-padding: 6 10; -fx-font-weight: 700; -fx-cursor: hand;");
        detailBtn.setOnAction(e -> { ViewContext.setSelectedEventId(ev.getId()); SceneUtil.switchScene("Events/EventDetail.fxml"); });

        stats.getChildren().addAll(likeBtn, commentLbl, attendeeLbl, spacer, actionBtn, detailBtn);
        body.getChildren().addAll(locLbl, stats);
        card.getChildren().addAll(header, body);
        return card;
    }

    @FXML private void onSearch() { if (showingUpcoming) loadUpcoming(); else loadMyEvents(); }
    @FXML private void onCalendar() { SceneUtil.switchScene("Events/EventsCalendar.fxml"); }

    @FXML private void handleDashboard()     { SceneUtil.switchScene("dashboard.fxml"); }
    @FXML private void handleJobOffers()     { SceneUtil.switchScene("OffersCardView.fxml"); }
    @FXML private void handleTrends()        { SceneUtil.switchScene("MarketTrendsView.fxml"); }
    @FXML private void handleInterviews()    { SceneUtil.switchScene("Interviews/InterviewView.fxml"); }
    @FXML private void handleCourses()       { SceneUtil.switchScene("Courses/CoursesBrowse.fxml"); }
    @FXML private void handleEvents()        { /* already here */ }
    @FXML private void handleMyCircle()      { SceneUtil.switchScene("my_circle.fxml"); }
    @FXML private void handleNotifications() { SceneUtil.switchScene("notifications.fxml"); }
    @FXML private void handleToDo()          { SceneUtil.switchScene("todo.fxml"); }
    @FXML private void handleActivities()    { SceneUtil.switchScene("activities/activities.fxml"); }
    @FXML private void handleProjects()      { SceneUtil.switchScene("projects/projects.fxml"); }
    @FXML private void handleMyProfile()     { SceneUtil.switchScene("profile-view.fxml"); }
    @FXML private void handleSettings()      { SceneUtil.switchScene("settings.fxml"); }
    @FXML private void handleLogout()        { AuthService.logout(); SceneUtil.switchScene("login.fxml"); }
}
