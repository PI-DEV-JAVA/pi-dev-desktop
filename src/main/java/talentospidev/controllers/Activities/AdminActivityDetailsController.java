package talentospidev.controllers.Activities;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import talentospidev.dao.ActivityDAO.ActivityDAO;
import talentospidev.dao.ActivityDAO.ActivityFileDAO;
import talentospidev.dao.ProjectDAO.ProjectDAO;
import talentospidev.models.Activity.Activity;
import talentospidev.models.Activity.ActivityFile;
import talentospidev.models.Project.Project;
import talentospidev.services.AuthService;
import talentospidev.utils.DB;
import talentospidev.utils.SceneUtil;
import talentospidev.utils.ViewContext;
import javafx.stage.FileChooser;

import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Recruiter admin view of an activity — see candidate details, uploaded files,
 * and time tracking information.
 */
public class AdminActivityDetailsController {

    @FXML
    private Label employeeNameLabel;
    @FXML
    private Label employeeEmailLabel;
    @FXML
    private Label activityIdLabel;
    @FXML
    private Label projectNameLabel;
    @FXML
    private Label dateLabel;
    @FXML
    private Label hoursLabel;
    @FXML
    private Label descriptionLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Label trackedTimeLabel;
    @FXML
    private Label completionLabel;
    @FXML
    private Label sessionsLabel;
    @FXML
    private Label lastActiveLabel;
    @FXML
    private Label currentStatusLabel;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private ListView<String> trackingHistoryList;
    @FXML
    private VBox filesContainer;
    @FXML
    private VBox mainContent;
    @FXML
    private ToggleButton trackLiveToggle;
    @FXML
    private HBox liveTrackingPanel;
    @FXML
    private VBox sidebar;
    @FXML
    private Button toDoTab;
    @FXML
    private Button activitiesTab;
    @FXML
    private Button projectsTab;

    private final ActivityDAO activityDAO = new ActivityDAO();
    private final ProjectDAO projectDAO = new ProjectDAO();
    private final ActivityFileDAO fileDAO = new ActivityFileDAO();
    private Activity currentActivity;

    private javafx.animation.Timeline refreshTimeline;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    private void initialize() {
        talentospidev.utils.SidebarUtil.configure(toDoTab, activitiesTab, projectsTab);
        int activityId = ViewContext.getSelectedActivityId();
        if (activityId > 0) {
            currentActivity = activityDAO.getById(activityId);
            if (currentActivity != null) {
                loadEmployeeInfo();
                displayActivityDetails();
                displayTrackingInfo();
                displayFiles();
                setupLiveTracking();
            }
        }
    }

    private void loadEmployeeInfo() {
        String sql = "SELECT u.email, p.first_name, p.last_name FROM users u LEFT JOIN profiles p ON p.user_id = u.id WHERE u.id = ?";
        try {
            Connection c = DB.getConnection();
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setInt(1, currentActivity.getEmployeeId());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String email = rs.getString("email");
                String fn = rs.getString("first_name");
                String ln = rs.getString("last_name");
                String name = (fn != null && ln != null) ? fn + " " + ln : email.split("@")[0];
                employeeNameLabel.setText(name);
                employeeEmailLabel.setText(email);
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void displayActivityDetails() {
        Project p = projectDAO.getById(currentActivity.getProjectId());
        activityIdLabel.setText("Activity #" + currentActivity.getIdActivity());
        projectNameLabel.setText(p != null ? p.getName() : "Unknown");
        dateLabel
                .setText("📅 " + currentActivity.getActivityDate().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
        hoursLabel.setText("⏱ " + String.format("%.1f hours", currentActivity.getHoursWorked()));
        descriptionLabel.setText(currentActivity.getDescription());

        if (p != null) {
            statusLabel.setText(p.getStatus());
            statusLabel.setStyle(getStatusStyle(p.getStatus()));
        }
    }

    private void displayTrackingInfo() {
        try {
            // Get tracking summary
            Map<String, Object> summary = activityDAO.getActivityTrackingSummary(currentActivity.getIdActivity());

            // Safely extract total_seconds
            Object secondsObj = summary.get("total_seconds");
            long totalSeconds = 0;
            if (secondsObj instanceof Long) {
                totalSeconds = (Long) secondsObj;
            } else if (secondsObj instanceof Integer) {
                totalSeconds = ((Integer) secondsObj).longValue();
            }

            // Safely extract total_sessions
            Object sessionsObj = summary.get("total_sessions");
            int sessionCount = 0;
            if (sessionsObj instanceof Long) {
                sessionCount = ((Long) sessionsObj).intValue();
            } else if (sessionsObj instanceof Integer) {
                sessionCount = (Integer) sessionsObj;
            }

            // Safely extract active_sessions
            Object activeObj = summary.get("active_sessions");
            int activeSessions = 0;
            if (activeObj instanceof Long) {
                activeSessions = ((Long) activeObj).intValue();
            } else if (activeObj instanceof Integer) {
                activeSessions = (Integer) activeObj;
            }

            // Calculate tracked hours
            double trackedHours = totalSeconds / 3600.0;
            double assignedHours = currentActivity.getHoursWorked();
            int completionPercent = (int) ((trackedHours / assignedHours) * 100);

            // Update labels
            trackedTimeLabel.setText(formatDuration(totalSeconds));
            completionLabel.setText(completionPercent + "%");
            sessionsLabel.setText(sessionCount + " session(s)");

            // Update progress bar
            progressBar.setProgress(Math.min(trackedHours / assignedHours, 1.0));
            progressBar.setStyle(getProgressBarStyle(completionPercent));

            // Current status
            if (currentActivity.isTracking()) {
                currentStatusLabel.setText("● LIVE");
                currentStatusLabel.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
            } else if (completionPercent >= 100) {
                currentStatusLabel.setText("✓ COMPLETED");
                currentStatusLabel.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
            } else if (activeSessions > 0) {
                currentStatusLabel.setText("⏸ PAUSED");
                currentStatusLabel.setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
            } else {
                currentStatusLabel.setText("○ NOT STARTED");
                currentStatusLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-weight: bold;");
            }

            // Last active
            if (summary.get("last_session") != null) {
                Timestamp lastSession = (Timestamp) summary.get("last_session");
                lastActiveLabel.setText(lastSession.toLocalDateTime().format(dateTimeFormatter));
            } else {
                lastActiveLabel.setText("Never");
            }

            // Load tracking history
            loadTrackingHistory();

        } catch (Exception e) {
            e.printStackTrace();
            // Set default values if there's an error
            trackedTimeLabel.setText("0h 0m");
            completionLabel.setText("0%");
            sessionsLabel.setText("0 sessions");
            currentStatusLabel.setText("○ UNKNOWN");
        }
    }

    private void loadTrackingHistory() {
        trackingHistoryList.getItems().clear();
        List<Map<String, Object>> history = activityDAO.getTrackingHistory(currentActivity.getIdActivity());

        if (history.isEmpty()) {
            trackingHistoryList.getItems().add("No tracking sessions yet");
            return;
        }

        for (Map<String, Object> session : history) {
            LocalDateTime start = (LocalDateTime) session.get("session_start");
            LocalDateTime end = (LocalDateTime) session.get("session_end");

            // Safely get seconds_tracked
            Object secondsObj = session.get("seconds_tracked");
            int seconds = 0;
            if (secondsObj instanceof Long) {
                seconds = ((Long) secondsObj).intValue();
            } else if (secondsObj instanceof Integer) {
                seconds = (Integer) secondsObj;
            }

            String startStr = start.format(DateTimeFormatter.ofPattern("dd/MM HH:mm"));
            String endStr = end != null ? end.format(DateTimeFormatter.ofPattern("HH:mm")) : "Active";
            String duration = formatDuration(seconds);

            // Add visual indicator for active session
            if (end == null) {
                trackingHistoryList.getItems().add(String.format("🟢 %s → %s (%s) - ACTIVE",
                        startStr, endStr, duration));
            } else {
                trackingHistoryList.getItems().add(String.format("⚪ %s → %s (%s)",
                        startStr, endStr, duration));
            }
        }
    }

    private void setupLiveTracking() {
        if (currentActivity.isTracking()) {
            liveTrackingPanel.setVisible(true);
            liveTrackingPanel.setManaged(true);
            trackLiveToggle.setSelected(true);
            trackLiveToggle.setText("Tracking Live");
            trackLiveToggle.setStyle("-fx-background-color: #10b981; -fx-text-fill: white;");
            startLiveRefresh();
        } else {
            liveTrackingPanel.setVisible(false);
            liveTrackingPanel.setManaged(false);
        }

        trackLiveToggle.setOnAction(e -> {
            if (trackLiveToggle.isSelected()) {
                startLiveRefresh();
                trackLiveToggle.setText("Tracking Live");
                trackLiveToggle.setStyle("-fx-background-color: #10b981; -fx-text-fill: white;");
            } else {
                stopLiveRefresh();
                trackLiveToggle.setText("Live Tracking Off");
                trackLiveToggle.setStyle("-fx-background-color: #6b7280; -fx-text-fill: white;");
            }
        });
    }

    private void startLiveRefresh() {
        if (refreshTimeline != null) {
            refreshTimeline.stop();
        }

        refreshTimeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(
                        javafx.util.Duration.seconds(5),
                        e -> refreshTrackingData()));
        refreshTimeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
        refreshTimeline.play();
    }

    private void stopLiveRefresh() {
        if (refreshTimeline != null) {
            refreshTimeline.stop();
        }
    }

    private void refreshTrackingData() {
        // Refresh activity data
        currentActivity = activityDAO.getById(currentActivity.getIdActivity());

        // Refresh tracking info
        displayTrackingInfo();
    }

    private String formatDuration(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, secs);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, secs);
        } else {
            return String.format("%ds", secs);
        }
    }

    private String getProgressBarStyle(int percent) {
        if (percent >= 100) {
            return "-fx-accent: #10b981;";
        } else if (percent >= 75) {
            return "-fx-accent: #f59e0b;";
        } else {
            return "-fx-accent: #ef4444;";
        }
    }

    private void displayFiles() {
        filesContainer.getChildren().clear();
        List<ActivityFile> files = fileDAO.getFilesByActivityId(currentActivity.getIdActivity());
        if (files.isEmpty()) {
            Label empty = new Label("No files attached");
            empty.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 12px;");
            filesContainer.getChildren().add(empty);
        } else {
            for (ActivityFile f : files)
                filesContainer.getChildren().add(createFileItem(f));
        }
    }

    private HBox createFileItem(ActivityFile file) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 12, 8, 12));
        row.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 8;");

        Label icon = new Label(getFileIcon(file.getFileType()));
        icon.setStyle("-fx-font-size: 16px;");
        VBox info = new VBox(1);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label name = new Label(file.getFileName());
        name.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: #111827;");
        Label size = new Label(file.getFormattedFileSize());
        size.setStyle("-fx-font-size: 10px; -fx-text-fill: #9ca3af;");
        info.getChildren().addAll(name, size);

        Button dlBtn = new Button("⬇");
        dlBtn.setStyle(
                "-fx-background-color: #eef2ff; -fx-text-fill: #6366f1; -fx-background-radius: 6; -fx-cursor: hand;");
        dlBtn.setOnAction(e -> downloadFile(file));

        row.getChildren().addAll(icon, info, dlBtn);
        return row;
    }

    private String getFileIcon(String type) {
        if (type == null)
            return "📄";
        return switch (type.toLowerCase()) {
            case "pdf" -> "📕";
            case "doc", "docx" -> "📘";
            case "png", "jpg", "jpeg" -> "🖼";
            case "zip", "rar" -> "📦";
            default -> "📄";
        };
    }

    private String getStatusStyle(String status) {
        return switch (status) {
            case "IN_PROGRESS" ->
                "-fx-background-color: #dbeafe; -fx-text-fill: #1e40af; -fx-padding: 4 12; -fx-background-radius: 10; -fx-font-size: 11px; -fx-font-weight: 700;";
            case "DONE" ->
                "-fx-background-color: #dcfce7; -fx-text-fill: #16a34a; -fx-padding: 4 12; -fx-background-radius: 10; -fx-font-size: 11px; -fx-font-weight: 700;";
            case "ON_HOLD" ->
                "-fx-background-color: #fef3c7; -fx-text-fill: #d97706; -fx-padding: 4 12; -fx-background-radius: 10; -fx-font-size: 11px; -fx-font-weight: 700;";
            default ->
                "-fx-background-color: #f3f4f6; -fx-text-fill: #6b7280; -fx-padding: 4 12; -fx-background-radius: 10; -fx-font-size: 11px; -fx-font-weight: 700;";
        };
    }

    private void downloadFile(ActivityFile file) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save File");
        chooser.setInitialFileName(file.getFileName());
        File dest = chooser.showSaveDialog(mainContent.getScene().getWindow());
        if (dest != null) {
            try {
                Files.copy(Path.of(file.getFilePath()), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                showAlert("Downloaded", "File saved to " + dest.getAbsolutePath(), Alert.AlertType.INFORMATION);
            } catch (IOException e) {
                showAlert("Error", "Download failed: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void downloadAllFiles() {
        List<ActivityFile> files = fileDAO.getFilesByActivityId(currentActivity.getIdActivity());
        if (files.isEmpty()) {
            showAlert("Info", "No files to download.", Alert.AlertType.INFORMATION);
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save ZIP");
        chooser.setInitialFileName("activity_" + currentActivity.getIdActivity() + "_files.zip");
        File dest = chooser.showSaveDialog(mainContent.getScene().getWindow());
        if (dest != null) {
            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(dest))) {
                for (ActivityFile f : files) {
                    File src = new File(f.getFilePath());
                    if (src.exists()) {
                        zos.putNextEntry(new ZipEntry(f.getFileName()));
                        Files.copy(src.toPath(), zos);
                        zos.closeEntry();
                    }
                }
                showAlert("Downloaded", "ZIP saved to " + dest.getAbsolutePath(), Alert.AlertType.INFORMATION);
            } catch (IOException e) {
                showAlert("Error", "ZIP failed: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void markAsReviewed() {
        if (currentActivity == null)
            return;
        String desc = currentActivity.getDescription();
        if (!desc.contains("[✅ REVIEWED]")) {
            currentActivity.setDescription("[✅ REVIEWED]\n" + desc);
            activityDAO.update(currentActivity);
            displayActivityDetails();
            showAlert("Marked", "Activity marked as reviewed!", Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    private void sendReminder() {
        showAlert("Reminder", "Reminder sent to " + employeeEmailLabel.getText(), Alert.AlertType.INFORMATION);
    }

    @FXML
    private void refreshData() {
        refreshTrackingData();
        showAlert("Refreshed", "Activity data has been refreshed.", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleBack() {
        stopLiveRefresh();
        SceneUtil.switchScene("activities/activities.fxml");
    }

    private void showAlert(String t, String m, Alert.AlertType type) {
        Alert a = new Alert(type);
        a.setTitle(t);
        a.setHeaderText(null);
        a.setContentText(m);
        a.showAndWait();
    }

    // === Sidebar Navigation ===
    @FXML
    private void handleDashboard() {
        stopLiveRefresh();
        SceneUtil.switchScene("recruiter_dashboard.fxml");
    }

    @FXML
    private void handleJobOffers() {
        stopLiveRefresh();
        SceneUtil.switchScene("OffersCardView.fxml");
    }
    @FXML private void handleTrends() { talentospidev.utils.SceneUtil.switchScene("MarketTrendsView.fxml"); }
    @FXML private void handleInterviews() { talentospidev.utils.SceneUtil.switchScene("Interviews/InterviewView.fxml"); }
    @javafx.fxml.FXML
    private void handleCourses() {
        talentospidev.models.User u = talentospidev.services.AuthService.getCurrentUser();
        boolean isRecruiter = u != null && (u.getRole() == talentospidev.models.User.Role.HR || u.getRole() == talentospidev.models.User.Role.ADMIN);
        talentospidev.utils.SceneUtil.switchScene(isRecruiter ? "Courses/CoursesRH.fxml" : "Courses/CoursesBrowse.fxml");
    }


    @FXML
    private void handleMyCircle() {
        stopLiveRefresh();
        SceneUtil.switchScene("my_circle.fxml");
    }

    @FXML
    private void handleNotifications() {
        stopLiveRefresh();
        SceneUtil.switchScene("notifications.fxml");
    }

    @FXML
    private void handleToDo() {
        stopLiveRefresh();
        SceneUtil.switchScene("activities/activity_employee.fxml");
    }

    @FXML
    private void handleActivities() {
        stopLiveRefresh();
        SceneUtil.switchScene("activities/activities.fxml");
    }

    @FXML
    private void handleProjects() {
        stopLiveRefresh();
        SceneUtil.switchScene("projects/projects.fxml");
    }

    @FXML
    private void handleMyProfile() {
        stopLiveRefresh();
        SceneUtil.switchScene("profile-view.fxml");
    }

    @FXML
    private void handleSettings() {
        stopLiveRefresh();
        SceneUtil.switchScene("settings.fxml");
    }

    @FXML
    private void handlePlaceholder() {
        new Alert(Alert.AlertType.INFORMATION, "Coming soon!").show();
    }

    @FXML
    private void handleLogout() {
        stopLiveRefresh();
        AuthService.logout();
        SceneUtil.switchScene("login.fxml");
    }
}