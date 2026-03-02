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
import talentospidev.utils.SceneUtil;
import talentospidev.utils.ViewContext;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Event statistics page — shows attendance rate, engagement, attendee list.
 */
public class EventStatsController {

    @FXML private Label titleLbl, subtitleLbl, attendeesLbl, confirmedLbl, likesLbl, commentsLbl;
    @FXML private VBox metricsContainer, attendeesListContainer, sidebar;
    @FXML private Button toDoTab, activitiesTab, projectsTab;

    private final EventDAO eventDAO = new EventDAO();
    private final EventParticipationDAO participationDAO = new EventParticipationDAO();
    private final EventLikeDAO likeDAO = new EventLikeDAO();
    private final EventCommentDAO commentDAO = new EventCommentDAO();
    private Event event;

    @FXML
    public void initialize() {
        talentospidev.utils.SidebarUtil.configure(toDoTab, activitiesTab, projectsTab);
        int evId = ViewContext.getSelectedEventId();
        if (evId > 0) {
            try {
                event = eventDAO.getById(evId);
                if (event != null) loadStats();
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void loadStats() throws Exception {
        subtitleLbl.setText(event.getTitle());

        int totalAttendees = participationDAO.countByEventId(event.getId());
        int confirmed = participationDAO.countConfirmed(event.getId());
        int attended = participationDAO.countAttended(event.getId());
        int likes = likeDAO.countByEventId(event.getId());
        int comments = commentDAO.countByEventId(event.getId());

        attendeesLbl.setText(String.valueOf(totalAttendees));
        confirmedLbl.setText(String.valueOf(confirmed));
        likesLbl.setText(String.valueOf(likes));
        commentsLbl.setText(String.valueOf(comments));

        // Engagement Metrics
        metricsContainer.getChildren().clear();

        // Attendance rate
        double attendanceRate = event.getMaxCapacity() > 0 ? (100.0 * totalAttendees / event.getMaxCapacity()) : 0;
        addMetricBar("Attendance Rate", attendanceRate, "#6366f1");

        // Engagement score (likes + comments per attendee)
        double engagementScore = totalAttendees > 0 ? ((double)(likes + comments) / totalAttendees * 100) : 0;
        addMetricBar("Engagement Score", Math.min(engagementScore, 100), "#22c55e");

        // Completion rate (attended / confirmed)
        double completionRate = confirmed > 0 ? (100.0 * attended / confirmed) : (totalAttendees > 0 ? 50 : 0);
        addMetricBar("Completion Rate", completionRate, "#f59e0b");

        // Social reach estimate
        double socialReach = Math.min((likes * 2.5 + comments * 5) * 10, 100);
        addMetricBar("Social Reach", socialReach, "#ec4899");

        // Attendees list
        attendeesListContainer.getChildren().clear();
        List<EventParticipation> participants = participationDAO.getByEventId(event.getId());
        for (EventParticipation p : participants) {
            HBox row = new HBox(12);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(8, 12, 8, 12));
            row.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 8;");

            Label avatar = new Label("👤");
            avatar.setStyle("-fx-font-size: 16px;");

            String email;
            try { User u = new UserDao().findById(p.getUserId()); email = u != null ? u.getEmail() : "User #" + p.getUserId(); } catch (Exception e) { email = "User #" + p.getUserId(); }
            Label nameLbl = new Label(email);
            nameLbl.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: #374151;");
            HBox.setHgrow(nameLbl, Priority.ALWAYS);

            Label statusLbl = new Label(p.getStatus());
            String badgeColor = switch (p.getStatus()) {
                case "CONFIRMED" -> "#dcfce7; -fx-text-fill: #166534";
                case "ATTENDED" -> "#dbeafe; -fx-text-fill: #1d4ed8";
                case "CANCELLED" -> "#fee2e2; -fx-text-fill: #dc2626";
                default -> "#f3f4f6; -fx-text-fill: #6b7280";
            };
            statusLbl.setStyle("-fx-background-color: " + badgeColor + "; -fx-background-radius: 6; -fx-padding: 3 8; -fx-font-size: 10px; -fx-font-weight: 700;");

            String date = p.getRegisteredAt() != null ? DateTimeFormatter.ofPattern("dd MMM HH:mm").format(p.getRegisteredAt()) : "";
            Label dateLbl = new Label(date);
            dateLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #d1d5db;");

            row.getChildren().addAll(avatar, nameLbl, statusLbl, dateLbl);
            attendeesListContainer.getChildren().add(row);
        }

        if (participants.isEmpty()) {
            Label emptyLbl = new Label("No attendees yet");
            emptyLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #9ca3af; -fx-padding: 12;");
            attendeesListContainer.getChildren().add(emptyLbl);
        }
    }

    private void addMetricBar(String label, double pct, String color) {
        VBox metricRow = new VBox(4);
        HBox labelRow = new HBox();
        labelRow.setAlignment(Pos.CENTER_LEFT);
        Label nameLbl = new Label(label);
        nameLbl.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: #374151;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Label valLbl = new Label(String.format("%.0f%%", pct));
        valLbl.setStyle("-fx-font-size: 12px; -fx-font-weight: 700; -fx-text-fill: " + color + ";");
        labelRow.getChildren().addAll(nameLbl, sp, valLbl);

        StackPane barBg = new StackPane();
        barBg.setStyle("-fx-background-color: #f3f4f6; -fx-background-radius: 6;");
        barBg.setPrefHeight(8);
        barBg.setMaxWidth(Double.MAX_VALUE);

        Region barFill = new Region();
        barFill.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 6;");
        barFill.setMaxHeight(8);
        barFill.setPrefHeight(8);
        barFill.maxWidthProperty().bind(barBg.widthProperty().multiply(Math.min(pct, 100) / 100.0));
        StackPane.setAlignment(barFill, Pos.CENTER_LEFT);
        barBg.getChildren().add(barFill);

        metricRow.getChildren().addAll(labelRow, barBg);
        metricsContainer.getChildren().add(metricRow);
    }

    @FXML private void onBack() { ViewContext.setSelectedEventId(event != null ? event.getId() : -1); SceneUtil.switchScene("Events/EventDetail.fxml"); }

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
