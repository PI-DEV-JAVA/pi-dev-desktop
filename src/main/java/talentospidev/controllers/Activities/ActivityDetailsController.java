package talentospidev.controllers.Activities;

import talentospidev.services.TrelloService;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.util.Duration;
import talentospidev.dao.ActivityDAO.ActivityDAO;
import talentospidev.dao.ActivityDAO.ActivityFileDAO;
import talentospidev.dao.ProjectDAO.ProjectDAO;
import talentospidev.models.Activity.Activity;
import talentospidev.models.Activity.ActivityFile;
import talentospidev.models.Project.Project;
import talentospidev.models.User;
import talentospidev.services.AuthService;
import talentospidev.utils.SceneUtil;
import talentospidev.utils.ViewContext;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Candidate activity detail page.
 * View assigned activity + upload files / write notes to update the recruiter.
 * Includes automatic time tracking based on user activity.
 */
public class ActivityDetailsController {

    @FXML
    private Label activityTitleLabel;
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
    private Label trackingStatusLabel;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private VBox filesContainer;
    @FXML
    private TextArea notesField;
    @FXML
    private VBox mainContent;
    @FXML
    private HBox trackingIndicator;
    @FXML
    private VBox sidebar;
    @FXML
    private Button toDoTab;
    @FXML
    private Button activitiesTab;
    @FXML
    private Button projectsTab;

    private final ActivityDAO activityDAO = new ActivityDAO();
    private final ActivityFileDAO fileDAO = new ActivityFileDAO();
    private final ProjectDAO projectDAO = new ProjectDAO();
    private Activity currentActivity;
    private static final String UPLOAD_DIR = System.getProperty("user.home") + "/talentos_uploads/";

    // Time tracking variables
    private Timeline trackingTimeline;
    private LocalDateTime sessionStartTime;
    private long sessionTrackedSeconds = 0;
    private LocalDateTime lastUserActivity = LocalDateTime.now();
    private static final int INACTIVITY_TIMEOUT_SECONDS = 300; // 5 minutes
    private boolean isTrackingActive = false;

    @FXML
    private void initialize() {
        talentospidev.utils.SidebarUtil.configure(toDoTab, activitiesTab, projectsTab);
        createUploadDirectory();
        int activityId = ViewContext.getSelectedActivityId();
        if (activityId > 0) {
            currentActivity = activityDAO.getById(activityId);
            if (currentActivity != null) {
                displayActivityDetails();
                loadFiles();
                setupInactivityDetection();
                startTracking(); // Auto-start tracking when view opens
            }
        }
    }

    private void createUploadDirectory() {
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
        } catch (IOException ignored) {
        }
    }

    private void displayActivityDetails() {
        Project p = projectDAO.getById(currentActivity.getProjectId());
        activityTitleLabel.setText("Activity #" + currentActivity.getIdActivity());
        projectNameLabel.setText(p != null ? p.getName() : "Unknown Project");
        dateLabel
                .setText("📅 " + currentActivity.getActivityDate().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
        hoursLabel.setText("⏱ " + String.format("%.1f hours", currentActivity.getHoursWorked()));
        descriptionLabel.setText(currentActivity.getDescription());

        if (p != null) {
            statusLabel.setText(p.getStatus());
            statusLabel.setStyle(getStatusStyle(p.getStatus()));
        }

        // Display tracked time
        updateTrackedTimeDisplay();
    }

    private void updateTrackedTimeDisplay() {
        if (trackedTimeLabel != null) {
            long totalSeconds = currentActivity.getTotalTrackedSeconds() + sessionTrackedSeconds;
            long hours = totalSeconds / 3600;
            long minutes = (totalSeconds % 3600) / 60;
            long seconds = totalSeconds % 60;

            trackedTimeLabel.setText(String.format("Tracked: %02d:%02d:%02d", hours, minutes, seconds));
        }

        if (progressBar != null) {
            double assignedHours = currentActivity.getHoursWorked();
            double trackedHours = (currentActivity.getTotalTrackedSeconds() + sessionTrackedSeconds) / 3600.0;
            double progress = assignedHours > 0 ? Math.min(trackedHours / assignedHours, 1.0) : 0;
            progressBar.setProgress(progress);

            // Color code progress
            if (progress >= 1.0) {
                progressBar.setStyle("-fx-accent: #10b981;");
            } else if (progress >= 0.75) {
                progressBar.setStyle("-fx-accent: #f59e0b;");
            } else {
                progressBar.setStyle("-fx-accent: #6366f1;");
            }
        }
    }

    private void setupInactivityDetection() {
        // Detect mouse movement on the main content
        mainContent.addEventFilter(MouseEvent.MOUSE_MOVED, e -> {
            if (isTrackingActive) {
                lastUserActivity = LocalDateTime.now();
                updateTrackingStatus("Active");
            }
        });

        mainContent.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
            if (isTrackingActive) {
                lastUserActivity = LocalDateTime.now();
                updateTrackingStatus("Active");
            }
        });

        mainContent.addEventFilter(MouseEvent.MOUSE_DRAGGED, e -> {
            if (isTrackingActive) {
                lastUserActivity = LocalDateTime.now();
                updateTrackingStatus("Active");
            }
        });

        // Detect keyboard activity
        mainContent.setOnKeyPressed(e -> {
            if (isTrackingActive) {
                lastUserActivity = LocalDateTime.now();
                updateTrackingStatus("Active");
            }
        });

        mainContent.setFocusTraversable(true);

        // Check for inactivity every 10 seconds
        Timeline inactivityChecker = new Timeline(
                new KeyFrame(Duration.seconds(10), e -> {
                    if (isTrackingActive) {
                        LocalDateTime now = LocalDateTime.now();
                        // Use java.time.Duration with fully qualified name
                        long idleSeconds = java.time.Duration.between(lastUserActivity, now).getSeconds();

                        if (idleSeconds > INACTIVITY_TIMEOUT_SECONDS) {
                            // Auto-pause tracking due to inactivity
                            pauseTracking();
                            showAlert("Inactivity Detected",
                                    "Tracking paused due to 5 minutes of inactivity. Move your mouse to resume.",
                                    Alert.AlertType.WARNING);
                        }
                    }
                }));
        inactivityChecker.setCycleCount(Animation.INDEFINITE);
        inactivityChecker.play();
    }

    private void startTracking() {
        if (!isTrackingActive) {
            sessionStartTime = LocalDateTime.now();
            sessionTrackedSeconds = 0;
            isTrackingActive = true;

            // Update activity status in database
            activityDAO.updateTrackingStatus(currentActivity.getIdActivity(), true, LocalDateTime.now());

            // Start the tracking timeline (updates every second)
            trackingTimeline = new Timeline(
                    new KeyFrame(Duration.seconds(1), e -> {
                        sessionTrackedSeconds++;
                        updateTrackedTimeDisplay();

                        // Auto-save every 30 seconds
                        if (sessionTrackedSeconds % 30 == 0) {
                            saveTrackingProgress();
                        }
                    }));
            trackingTimeline.setCycleCount(Animation.INDEFINITE);
            trackingTimeline.play();

            updateTrackingStatus("Tracking Active");
            System.out.println("✅ Tracking started for activity " + currentActivity.getIdActivity());
        }
    }

    private void pauseTracking() {
        if (isTrackingActive) {
            // Save current session progress
            saveTrackingProgress();

            // Stop the timeline
            if (trackingTimeline != null) {
                trackingTimeline.stop();
            }

            isTrackingActive = false;
            activityDAO.updateTrackingStatus(currentActivity.getIdActivity(), false, LocalDateTime.now());

            updateTrackingStatus("Paused");
            System.out.println("⏸️ Tracking paused for activity " + currentActivity.getIdActivity());
        }
    }

    private void resumeTracking() {
        if (!isTrackingActive) {
            sessionStartTime = LocalDateTime.now();
            sessionTrackedSeconds = 0;
            isTrackingActive = true;

            activityDAO.updateTrackingStatus(currentActivity.getIdActivity(), true, LocalDateTime.now());

            // Restart timeline
            trackingTimeline = new Timeline(
                    new KeyFrame(Duration.seconds(1), e -> {
                        sessionTrackedSeconds++;
                        updateTrackedTimeDisplay();

                        if (sessionTrackedSeconds % 30 == 0) {
                            saveTrackingProgress();
                        }
                    }));
            trackingTimeline.setCycleCount(Animation.INDEFINITE);
            trackingTimeline.play();

            updateTrackingStatus("Tracking Active");
            System.out.println("▶️ Tracking resumed for activity " + currentActivity.getIdActivity());
        }
    }

    private void stopTracking() {
        if (isTrackingActive || sessionTrackedSeconds > 0) {
            // Save the final session
            if (sessionStartTime != null && sessionTrackedSeconds > 0) {
                LocalDateTime now = LocalDateTime.now();

                // Save to tracking history
                activityDAO.saveTrackingSession(
                        currentActivity.getIdActivity(),
                        sessionStartTime,
                        now,
                        (int) sessionTrackedSeconds);

                System.out.println("💾 Saved tracking session: " + sessionTrackedSeconds + " seconds");
            }

            // Stop timeline
            if (trackingTimeline != null) {
                trackingTimeline.stop();
            }

            // Update total tracked time
            activityDAO.updateTotalTrackedTime(currentActivity.getIdActivity());

            // Update activity status
            isTrackingActive = false;
            activityDAO.updateTrackingStatus(currentActivity.getIdActivity(), false, LocalDateTime.now());

            // Reload activity to get updated totals
            currentActivity = activityDAO.getById(currentActivity.getIdActivity());

            sessionTrackedSeconds = 0;
            sessionStartTime = null;

            updateTrackedTimeDisplay();
            updateTrackingStatus("Stopped");
            System.out.println("⏹️ Tracking stopped for activity " + currentActivity.getIdActivity());
        }
    }

    private void saveTrackingProgress() {
        if (sessionTrackedSeconds > 0 && sessionStartTime != null) {
            // Save current progress to history
            LocalDateTime now = LocalDateTime.now();

            activityDAO.saveTrackingSession(
                    currentActivity.getIdActivity(),
                    sessionStartTime,
                    now,
                    (int) sessionTrackedSeconds);

            // Update total in activities table
            activityDAO.updateTotalTrackedTime(currentActivity.getIdActivity());

            // Reload activity to get updated totals
            Activity updatedActivity = activityDAO.getById(currentActivity.getIdActivity());

            // Calculate progress percentage
            double oldTrackedHours = currentActivity.getTrackedHours();
            double newTrackedHours = updatedActivity.getTrackedHours();
            double progressPercent = (newTrackedHours / currentActivity.getHoursWorked()) * 100;

            // Update current activity with new values
            currentActivity.setTotalTrackedSeconds(updatedActivity.getTotalTrackedSeconds());

            // Update Trello if progress crossed a milestone
            if (Math.floor(oldTrackedHours) != Math.floor(newTrackedHours)) {
                // Get the card ID from somewhere (you'd need to store it)
                String cardId = getTrelloCardIdForActivity(currentActivity.getIdActivity());
                if (cardId != null) {
                    TrelloService.updateActivityCard(cardId, currentActivity, progressPercent);
                }
            }

            // Reset session start for next segment
            sessionStartTime = now;
            sessionTrackedSeconds = 0;

            // Update display
            updateTrackedTimeDisplay();

            System.out.println("💾 Auto-saved tracking progress: " +
                    String.format("%.1f/%.1f hours (%.0f%%)",
                            newTrackedHours, currentActivity.getHoursWorked(), progressPercent));
        }
    }

    // Helper method to get Trello card ID for an activity
    // You need to store this in a database table when creating the Trello card
    private String getTrelloCardIdForActivity(int activityId) {
        // TODO: Implement this by querying a table that stores activity_id ->
        // trello_card_id mapping
        // For now, this returns null and Trello won't be updated
        return null;
    }

    private void updateTrackingStatus(String status) {
        if (trackingStatusLabel != null) {
            trackingStatusLabel.setText(status);

            switch (status) {
                case "Tracking Active":
                    trackingStatusLabel.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
                    break;
                case "Paused":
                    trackingStatusLabel.setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                    break;
                case "Stopped":
                    trackingStatusLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-weight: bold;");
                    break;
                default:
                    trackingStatusLabel.setStyle("-fx-text-fill: #6b7280;");
            }
        }

        if (trackingIndicator != null) {
            if (status.equals("Tracking Active")) {
                trackingIndicator.setStyle("-fx-background-color: #10b981; -fx-background-radius: 5;");
            } else {
                trackingIndicator.setStyle("-fx-background-color: #9ca3af; -fx-background-radius: 5;");
            }
        }
    }

    private void loadFiles() {
        filesContainer.getChildren().clear();
        List<ActivityFile> files = fileDAO.getFilesByActivityId(currentActivity.getIdActivity());
        if (files.isEmpty()) {
            Label empty = new Label("No files uploaded yet");
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
        icon.setStyle("-fx-font-size: 18px;");
        VBox info = new VBox(1);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label name = new Label(file.getFileName());
        name.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: #111827;");
        Label size = new Label(file.getFormattedFileSize());
        size.setStyle("-fx-font-size: 10px; -fx-text-fill: #9ca3af;");
        info.getChildren().addAll(name, size);

        Button delBtn = new Button("🗑");
        delBtn.setStyle(
                "-fx-background-color: #fef2f2; -fx-text-fill: #ef4444; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 10px;");
        delBtn.setOnAction(e -> deleteFile(file));

        row.getChildren().addAll(icon, info, delBtn);
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

    @FXML
    private void uploadFile() {
        if (currentActivity == null)
            return;
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Upload File");
        File file = chooser.showOpenDialog(mainContent.getScene().getWindow());
        if (file == null)
            return;

        try {
            String destPath = UPLOAD_DIR + "act_" + currentActivity.getIdActivity() + "_" + file.getName();
            Files.copy(file.toPath(), Path.of(destPath), StandardCopyOption.REPLACE_EXISTING);

            String ext = file.getName().contains(".") ? file.getName().substring(file.getName().lastIndexOf('.') + 1)
                    : "";
            ActivityFile af = new ActivityFile(currentActivity.getIdActivity(), file.getName(), destPath, file.length(),
                    ext);
            fileDAO.addFile(af);
            loadFiles();
            showAlert("Success", "File uploaded!", Alert.AlertType.INFORMATION);
        } catch (IOException e) {
            showAlert("Error", "Failed to upload: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void deleteFile(ActivityFile file) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete " + file.getFileName() + "?", ButtonType.YES,
                ButtonType.NO);
        if (confirm.showAndWait().orElse(null) == ButtonType.YES) {
            try {
                Files.deleteIfExists(Path.of(file.getFilePath()));
            } catch (IOException ignored) {
            }
            fileDAO.deleteFile(file.getId());
            loadFiles();
        }
    }

    @FXML
    private void saveNotes() {
        if (currentActivity == null || notesField.getText().trim().isEmpty())
            return;
        String updated = currentActivity.getDescription() + "\n\n--- Update from candidate ---\n"
                + notesField.getText().trim();
        currentActivity.setDescription(updated);
        activityDAO.update(currentActivity);
        notesField.clear();
        displayActivityDetails();
        showAlert("Saved", "Your update has been sent to the recruiter!", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleBack() {
        // Stop tracking when navigating away
        stopTracking();
        SceneUtil.switchScene("activities/activity_employee.fxml");
    }

    private void showAlert(String title, String msg, Alert.AlertType type) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private String getStatusStyle(String status) {
        return switch (status) {
            case "IN_PROGRESS" ->
                "-fx-background-color: #dbeafe; -fx-text-fill: #1e40af; -fx-padding: 3 10; -fx-background-radius: 10; -fx-font-size: 10px; -fx-font-weight: 700;";
            case "DONE" ->
                "-fx-background-color: #dcfce7; -fx-text-fill: #16a34a; -fx-padding: 3 10; -fx-background-radius: 10; -fx-font-size: 10px; -fx-font-weight: 700;";
            case "ON_HOLD" ->
                "-fx-background-color: #fef3c7; -fx-text-fill: #d97706; -fx-padding: 3 10; -fx-background-radius: 10; -fx-font-size: 10px; -fx-font-weight: 700;";
            default ->
                "-fx-background-color: #f3f4f6; -fx-text-fill: #6b7280; -fx-padding: 3 10; -fx-background-radius: 10; -fx-font-size: 10px; -fx-font-weight: 700;";
        };
    }

    // === Sidebar Navigation ===
    @FXML
    private void handleDashboard() {
        stopTracking(); // Stop tracking before navigating away
        SceneUtil.switchScene("dashboard.fxml");
    }

    @FXML
    private void handleJobOffers() {
        stopTracking(); // Stop tracking before navigating away
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
        stopTracking();
        SceneUtil.switchScene("my_circle.fxml");
    }

    @FXML
    private void handleNotifications() {
        stopTracking();
        SceneUtil.switchScene("notifications.fxml");
    }

    @FXML
    private void handleActivities() {
        stopTracking();
        SceneUtil.switchScene("activities/activities.fxml");
    }

    @FXML
    private void handleProjects() {
        stopTracking();
        SceneUtil.switchScene("projects/projects.fxml");
    }

    @FXML
    private void handleToDo() {
        stopTracking(); // Stop tracking before navigating away
        SceneUtil.switchScene("activities/activity_employee.fxml");
    }

    @FXML
    private void handleMyProfile() {
        stopTracking(); // Stop tracking before navigating away
        SceneUtil.switchScene("profile-view.fxml");
    }

    @FXML
    private void handleSettings() {
        stopTracking();
        SceneUtil.switchScene("settings.fxml");
    }

    @FXML
    private void handlePlaceholder() {
        new Alert(Alert.AlertType.INFORMATION, "Coming soon!").show();
    }

    @FXML
    private void handleLogout() {
        stopTracking(); // Stop tracking before logout
        AuthService.logout();
        SceneUtil.switchScene("login.fxml");
    }
}