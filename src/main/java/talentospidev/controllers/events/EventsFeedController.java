package talentospidev.controllers.events;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import talentospidev.dao.eventsDAO.*;
import talentospidev.models.User;
import talentospidev.models.events.Event;
import talentospidev.services.AuthService;
import talentospidev.utils.SceneUtil;
import talentospidev.utils.ViewContext;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class EventsFeedController {

    @FXML private VBox eventsContainer, sidebar;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterBox;
    @FXML private Button toDoTab, activitiesTab, projectsTab;

    private final EventDAO eventDAO = new EventDAO();
    private final EventLikeDAO likeDAO = new EventLikeDAO();
    private final EventCommentDAO commentDAO = new EventCommentDAO();
    private final EventParticipationDAO participationDAO = new EventParticipationDAO();
    private static final DateTimeFormatter dtFmt = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    @FXML
    public void initialize() {
        talentospidev.utils.SidebarUtil.configure(toDoTab, activitiesTab, projectsTab);
        filterBox.setItems(FXCollections.observableArrayList("All", "MEETUP", "CONFERENCE", "WORKSHOP", "WEBINAR"));
        filterBox.setValue("All");
        filterBox.setOnAction(e -> loadEvents());
        loadEvents();
    }

    private void loadEvents() {
        eventsContainer.getChildren().clear();
        try {
            User u = AuthService.getCurrentUser();
            List<Event> events = eventDAO.getAll();

            String q = searchField.getText() == null ? "" : searchField.getText().toLowerCase();
            String filter = filterBox.getValue();

            for (Event ev : events) {
                if (!q.isEmpty() && (ev.getTitle() == null || !ev.getTitle().toLowerCase().contains(q))) continue;
                if (!"All".equals(filter) && !filter.equals(ev.getEventType())) continue;

                VBox card = buildEventCard(ev, u);
                eventsContainer.getChildren().add(card);
            }

            if (eventsContainer.getChildren().isEmpty()) {
                Label emptyLbl = new Label("No events found. Create your first event! 🎉");
                emptyLbl.setStyle("-fx-font-size: 16px; -fx-text-fill: #9ca3af; -fx-padding: 40;");
                eventsContainer.getChildren().add(emptyLbl);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private VBox buildEventCard(Event ev, User u) throws Exception {
        VBox card = new VBox(0);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-border-color: #e5e7eb; -fx-border-radius: 16; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 3);");
        card.setOnMouseEntered(e -> card.setStyle(card.getStyle().replace("rgba(0,0,0,0.05)", "rgba(99,102,241,0.1)")));
        card.setOnMouseExited(e -> card.setStyle(card.getStyle().replace("rgba(99,102,241,0.1)", "rgba(0,0,0,0.05)")));

        // Gradient header
        String[] gradients = {"#6366f1,#8b5cf6", "#ec4899,#f43f5e", "#14b8a6,#06b6d4", "#f59e0b,#ef4444"};
        String grad = gradients[Math.abs(ev.getTitle().hashCode()) % gradients.length];
        HBox header = new HBox();
        header.setStyle("-fx-background-color: linear-gradient(to right, " + grad + "); -fx-background-radius: 16 16 0 0; -fx-padding: 20 24;");
        header.setAlignment(Pos.CENTER_LEFT);

        VBox headerInfo = new VBox(4);
        HBox.setHgrow(headerInfo, Priority.ALWAYS);

        Label typeBadge = new Label(ev.getEventType() != null ? ev.getEventType() : "EVENT");
        typeBadge.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-background-radius: 8; " +
                "-fx-padding: 3 10; -fx-font-size: 10px; -fx-font-weight: 700;");
        Label titleLbl = new Label(ev.getTitle());
        titleLbl.setStyle("-fx-font-size: 18px; -fx-font-weight: 800; -fx-text-fill: white;");
        titleLbl.setWrapText(true);

        String dateStr = ev.getEventDate() != null ? dtFmt.format(ev.getEventDate()) : "";
        Label dateLbl = new Label("📅 " + dateStr);
        dateLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(255,255,255,0.85);");

        headerInfo.getChildren().addAll(typeBadge, titleLbl, dateLbl);
        header.getChildren().add(headerInfo);

        // Body
        VBox body = new VBox(10);
        body.setPadding(new Insets(16, 24, 16, 24));

        // Description preview
        if (ev.getDescription() != null && !ev.getDescription().isEmpty()) {
            String preview = ev.getDescription().length() > 120 ? ev.getDescription().substring(0, 120) + "..." : ev.getDescription();
            Label descLbl = new Label(preview);
            descLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");
            descLbl.setWrapText(true);
            body.getChildren().add(descLbl);
        }

        // Location
        String locText = ev.isOnline() ? "🌐 Online Event" : ("📍 " + (ev.getLocation() != null ? ev.getLocation() : "TBD"));
        Label locLbl = new Label(locText);
        locLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af;");
        body.getChildren().add(locLbl);

        // Stats row
        int likes = likeDAO.countByEventId(ev.getId());
        int comments = commentDAO.countByEventId(ev.getId());
        int attendees = participationDAO.countByEventId(ev.getId());
        boolean liked = u != null && likeDAO.hasLiked(ev.getId(), u.getId());

        HBox stats = new HBox(16);
        stats.setAlignment(Pos.CENTER_LEFT);
        stats.setPadding(new Insets(8, 0, 0, 0));

        Button likeBtn = new Button((liked ? "❤️ " : "🤍 ") + likes);
        likeBtn.setStyle("-fx-background-color: " + (liked ? "#fee2e2" : "#f3f4f6") + "; " +
                "-fx-text-fill: " + (liked ? "#dc2626" : "#6b7280") + "; -fx-background-radius: 8; " +
                "-fx-padding: 6 12; -fx-font-size: 12px; -fx-font-weight: 700; -fx-cursor: hand;");
        likeBtn.setOnAction(e -> {
            try {
                if (u != null) { likeDAO.toggle(ev.getId(), u.getId()); loadEvents(); }
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        Label commentLbl = new Label("💬 " + comments);
        commentLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280; -fx-font-weight: 600;");

        Label attendeeLbl = new Label("👥 " + attendees + (ev.getMaxCapacity() > 0 ? "/" + ev.getMaxCapacity() : ""));
        attendeeLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280; -fx-font-weight: 600;");

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

        Button viewBtn = new Button("View Details →");
        viewBtn.setStyle("-fx-background-color: #6366f1; -fx-text-fill: white; -fx-background-radius: 8; " +
                "-fx-padding: 6 16; -fx-font-size: 12px; -fx-font-weight: 700; -fx-cursor: hand;");
        viewBtn.setOnAction(e -> { ViewContext.setSelectedEventId(ev.getId()); SceneUtil.switchScene("Events/EventDetail.fxml"); });

        // Edit/Delete for organizer
        HBox adminBtns = new HBox(6);
        if (u != null && (u.getId() == ev.getOrganizerId() || u.getRole() == User.Role.ADMIN)) {
            Button editBtn = new Button("✏");
            editBtn.setStyle("-fx-background-color: #fef3c7; -fx-text-fill: #d97706; -fx-background-radius: 6; -fx-padding: 4 8; -fx-cursor: hand;");
            editBtn.setOnAction(e -> { ViewContext.setSelectedEventId(ev.getId()); SceneUtil.switchScene("Events/EventForm.fxml"); });
            Button delBtn = new Button("🗑");
            delBtn.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-background-radius: 6; -fx-padding: 4 8; -fx-cursor: hand;");
            delBtn.setOnAction(e -> {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete this event?");
                confirm.showAndWait().ifPresent(r -> {
                    if (r == ButtonType.OK) { try { eventDAO.delete(ev.getId()); loadEvents(); } catch (Exception ex) { ex.printStackTrace(); } }
                });
            });
            adminBtns.getChildren().addAll(editBtn, delBtn);
        }

        stats.getChildren().addAll(likeBtn, commentLbl, attendeeLbl, spacer, adminBtns, viewBtn);
        body.getChildren().add(stats);

        card.getChildren().addAll(header, body);
        return card;
    }

    @FXML private void onCreate() { ViewContext.setSelectedEventId(-1); SceneUtil.switchScene("Events/EventForm.fxml"); }
    @FXML private void onSearch() { loadEvents(); }

    // Sidebar navigation
    @FXML private void handleDashboard()     { SceneUtil.switchScene("dashboard.fxml"); }
    @FXML private void handleJobOffers()     { SceneUtil.switchScene("OffersCardView.fxml"); }
    @FXML private void handleTrends()        { SceneUtil.switchScene("MarketTrendsView.fxml"); }
    @FXML private void handleInterviews()    { SceneUtil.switchScene("Interviews/InterviewView.fxml"); }
    @FXML private void handleCourses() {
        User u = AuthService.getCurrentUser();
        if (u != null && u.getRole() == User.Role.CANDIDATE) SceneUtil.switchScene("Courses/CoursesBrowse.fxml");
        else SceneUtil.switchScene("Courses/CoursesRH.fxml");
    }
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
