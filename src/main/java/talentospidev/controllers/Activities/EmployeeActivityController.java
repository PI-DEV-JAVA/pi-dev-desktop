package talentospidev.controllers.Activities;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import talentospidev.dao.ActivityDAO.ActivityDAO;
import talentospidev.dao.ProjectDAO.ProjectDAO;
import talentospidev.models.Activity.Activity;
import talentospidev.models.Project.Project;
import talentospidev.models.User;
import talentospidev.services.AuthService;
import talentospidev.utils.SceneUtil;
import talentospidev.utils.ViewContext;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Candidate "To Do" view — shows activities assigned to the current candidate.
 * Now includes time tracking information and progress indicators.
 */
public class EmployeeActivityController {

    @FXML
    private Label welcomeLabel;
    @FXML
    private Label totalHoursLabel;
    @FXML
    private Label totalActivitiesLabel;
    @FXML
    private Label projectsCountLabel;
    @FXML
    private Label avgHoursLabel;
    @FXML
    private Label activitiesCountLabel;
    @FXML
    private Label totalTrackedLabel;
    @FXML
    private Label completionRateLabel;
    @FXML
    private ProgressBar overallProgressBar;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> filterProjectCombo;
    @FXML
    private ComboBox<String> filterStatusCombo;
    @FXML
    private DatePicker filterDatePicker;
    @FXML
    private Button clearFilterBtn;
    @FXML
    private VBox activitiesContainer;
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
    private ObservableList<Activity> masterData = FXCollections.observableArrayList();
    private FilteredList<Activity> filteredData;
    private final Map<Integer, String> projectMap = new HashMap<>();
    private final Map<Integer, String> projectStatusMap = new HashMap<>();
    private final Map<Integer, LocalDate> projectDeadlines = new HashMap<>();
    private final Map<Integer, Long> trackedSecondsMap = new HashMap<>();

    private javafx.animation.Timeline refreshTimeline;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private final DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    @FXML
    private void initialize() {
        try {
            talentospidev.utils.SidebarUtil.configure(toDoTab, activitiesTab, projectsTab);
            User user = AuthService.getCurrentUser();
            if (user == null) {
                SceneUtil.switchScene("login.fxml");
                return;
            }

            welcomeLabel.setText("Welcome back, " + user.getEmail().split("@")[0] + "! 👋");

            loadProjectsData();
            setupFilters();
            loadUserActivities(user.getId());

            clearFilterBtn.setOnAction(e -> clearFilters());

            // Start auto-refresh every 30 seconds
            startAutoRefresh();

            System.out.println("EmployeeActivityController initialized successfully");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Initialization Error", "Error loading activities: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadProjectsData() {
        try {
            List<Project> projects = projectDAO.getAll();
            ObservableList<String> projectNames = FXCollections.observableArrayList();
            projectNames.add("All Projects");
            for (Project p : projects) {
                projectMap.put(p.getId(), p.getName());
                projectStatusMap.put(p.getId(), p.getStatus());
                if (p.getEndDate() != null)
                    projectDeadlines.put(p.getId(), p.getEndDate());
                projectNames.add(p.getName());
            }

            if (filterProjectCombo != null) {
                filterProjectCombo.setItems(projectNames);
                filterProjectCombo.getSelectionModel().selectFirst();
            }

            // Add status filter
            if (filterStatusCombo != null) {
                filterStatusCombo.setItems(FXCollections.observableArrayList(
                        "All Status", "Not Started", "In Progress", "Completed", "Overdue"));
                filterStatusCombo.getSelectionModel().selectFirst();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load projects: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadUserActivities(int userId) {
        try {
            List<Activity> activities = activityDAO.getByEmployeeId(userId);
            masterData.setAll(activities);

            // Load tracked times for all activities
            for (Activity a : activities) {
                Map<String, Object> summary = activityDAO.getActivityTrackingSummary(a.getIdActivity());
                Object secondsObj = summary.get("total_seconds");
                long totalSeconds = 0;
                if (secondsObj instanceof Long) {
                    totalSeconds = (Long) secondsObj;
                } else if (secondsObj instanceof Integer) {
                    totalSeconds = ((Integer) secondsObj).longValue();
                }
                trackedSecondsMap.put(a.getIdActivity(), totalSeconds);
            }

            filteredData = new FilteredList<>(masterData, p -> true);
            displayActivities();
            updateStatistics();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load activities: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void startAutoRefresh() {
        try {
            // Stop any existing timeline
            if (refreshTimeline != null) {
                refreshTimeline.stop();
            }

            // Create a timeline that refreshes every 30 seconds
            refreshTimeline = new javafx.animation.Timeline(
                    new javafx.animation.KeyFrame(
                            javafx.util.Duration.seconds(30),
                            e -> refreshData()));
            refreshTimeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
            refreshTimeline.play();
            System.out.println("Auto-refresh started");
        } catch (Exception e) {
            System.err.println("Error starting auto-refresh: " + e.getMessage());
        }
    }

    private void stopAutoRefresh() {
        if (refreshTimeline != null) {
            refreshTimeline.stop();
            System.out.println("Auto-refresh stopped");
        }
    }

    private void setupFilters() {
        if (searchField != null) {
            searchField.textProperty().addListener((o, ov, nv) -> updateFilter());
        }
        if (filterProjectCombo != null) {
            filterProjectCombo.valueProperty().addListener((o, ov, nv) -> updateFilter());
        }
        if (filterStatusCombo != null) {
            filterStatusCombo.valueProperty().addListener((o, ov, nv) -> updateFilter());
        }
        if (filterDatePicker != null) {
            filterDatePicker.valueProperty().addListener((o, ov, nv) -> updateFilter());
        }
    }

    private void updateFilter() {
        if (filteredData == null)
            return;

        filteredData.setPredicate(activity -> {
            try {
                // Search filter
                if (searchField.getText() != null && !searchField.getText().isEmpty()) {
                    if (!activity.getDescription().toLowerCase().contains(searchField.getText().toLowerCase()))
                        return false;
                }

                // Project filter
                String selProject = filterProjectCombo.getValue();
                if (selProject != null && !selProject.equals("All Projects")) {
                    String pn = projectMap.get(activity.getProjectId());
                    if (!selProject.equals(pn))
                        return false;
                }

                // Status filter
                String selStatus = filterStatusCombo.getValue();
                if (selStatus != null && !selStatus.equals("All Status")) {
                    String activityStatus = getActivityStatus(activity);
                    if (!selStatus.equals(activityStatus))
                        return false;
                }

                // Date filter
                LocalDate fd = filterDatePicker.getValue();
                if (fd != null && !activity.getActivityDate().equals(fd))
                    return false;

                return true;
            } catch (Exception e) {
                return false;
            }
        });

        displayActivities();
        updateStatistics();
    }

    private String getActivityStatus(Activity activity) {
        try {
            long tracked = trackedSecondsMap.getOrDefault(activity.getIdActivity(), 0L);
            double assignedHours = activity.getHoursWorked();
            double trackedHours = tracked / 3600.0;

            LocalDate deadline = projectDeadlines.get(activity.getProjectId());

            if (deadline != null && deadline.isBefore(LocalDate.now()) && trackedHours < assignedHours) {
                return "Overdue";
            } else if (trackedHours >= assignedHours) {
                return "Completed";
            } else if (trackedHours > 0) {
                return "In Progress";
            } else {
                return "Not Started";
            }
        } catch (Exception e) {
            return "Unknown";
        }
    }

    private void clearFilters() {
        searchField.clear();
        filterProjectCombo.getSelectionModel().selectFirst();
        filterStatusCombo.getSelectionModel().selectFirst();
        filterDatePicker.setValue(null);
    }

    private void displayActivities() {
        activitiesContainer.getChildren().clear();
        if (filteredData == null || filteredData.isEmpty()) {
            showEmptyState();
            return;
        }

        List<Activity> sorted = new ArrayList<>(filteredData);
        sorted.sort((a, b) -> {
            // Sort by status first (active first, then overdue, then others)
            String statusA = getActivityStatus(a);
            String statusB = getActivityStatus(b);

            if (statusA.equals("In Progress") && !statusB.equals("In Progress"))
                return -1;
            if (!statusA.equals("In Progress") && statusB.equals("In Progress"))
                return 1;
            if (statusA.equals("Overdue") && !statusB.equals("Overdue"))
                return -1;
            if (!statusA.equals("Overdue") && statusB.equals("Overdue"))
                return 1;

            // Then by date (most recent first)
            return b.getActivityDate().compareTo(a.getActivityDate());
        });

        for (Activity a : sorted)
            activitiesContainer.getChildren().add(createActivityCard(a));

        if (activitiesCountLabel != null)
            activitiesCountLabel.setText("Showing " + filteredData.size() + " activity(ies)");
    }

    private void showEmptyState() {
        VBox empty = new VBox(16);
        empty.setAlignment(Pos.CENTER);
        empty.setPadding(new Insets(40));
        empty.setStyle("-fx-background-color: white; -fx-background-radius: 14;");

        Label icon = new Label("📭");
        icon.setStyle("-fx-font-size: 48px;");

        Label title = new Label("No Activities Found");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #111827;");

        Label desc = new Label("No tasks have been assigned to you yet.");
        desc.setStyle("-fx-font-size: 13px; -fx-text-fill: #6b7280;");

        empty.getChildren().addAll(icon, title, desc);
        activitiesContainer.getChildren().add(empty);

        if (activitiesCountLabel != null)
            activitiesCountLabel.setText("No activities");
    }

    private VBox createActivityCard(Activity activity) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(18));

        // Get tracking info
        long trackedSeconds = trackedSecondsMap.getOrDefault(activity.getIdActivity(), 0L);
        double trackedHours = trackedSeconds / 3600.0;
        double assignedHours = activity.getHoursWorked();
        int completionPercent = assignedHours > 0 ? (int) ((trackedHours / assignedHours) * 100) : 0;
        String status = getActivityStatus(activity);

        // Create base styles as final variables
        final String baseStyle = "-fx-background-color: white; -fx-background-radius: 14; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 6, 0, 0, 2); -fx-cursor: hand;";

        final String hoverStyle = "-fx-background-color: #fafbff; -fx-background-radius: 14; " +
                "-fx-effect: dropshadow(gaussian, rgba(99,102,241,0.12), 8, 0, 0, 3); -fx-cursor: hand;";

        // Create border styles based on status (as final variables)
        final String borderStyle;
        final String hoverBorderStyle;

        if (status.equals("Overdue")) {
            borderStyle = baseStyle
                    + " -fx-border-color: #ef4444; -fx-border-width: 0 0 0 4; -fx-border-radius: 14 0 0 14;";
            hoverBorderStyle = hoverStyle
                    + " -fx-border-color: #ef4444; -fx-border-width: 0 0 0 4; -fx-border-radius: 14 0 0 14;";
        } else if (status.equals("In Progress")) {
            borderStyle = baseStyle
                    + " -fx-border-color: #6366f1; -fx-border-width: 0 0 0 4; -fx-border-radius: 14 0 0 14;";
            hoverBorderStyle = hoverStyle
                    + " -fx-border-color: #6366f1; -fx-border-width: 0 0 0 4; -fx-border-radius: 14 0 0 14;";
        } else if (status.equals("Completed")) {
            borderStyle = baseStyle
                    + " -fx-border-color: #10b981; -fx-border-width: 0 0 0 4; -fx-border-radius: 14 0 0 14;";
            hoverBorderStyle = hoverStyle
                    + " -fx-border-color: #10b981; -fx-border-width: 0 0 0 4; -fx-border-radius: 14 0 0 14;";
        } else {
            borderStyle = baseStyle;
            hoverBorderStyle = hoverStyle;
        }

        card.setStyle(borderStyle);
        card.setOnMouseEntered(e -> card.setStyle(hoverBorderStyle));
        card.setOnMouseExited(e -> card.setStyle(borderStyle));

        // Click → navigate to activity details
        card.setOnMouseClicked(e -> {
            ViewContext.setSelectedActivityId(activity.getIdActivity());
            SceneUtil.switchScene("activities/activity_details.fxml");
        });

        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        // Date badge
        VBox dateBadge = new VBox(2);
        dateBadge.setStyle("-fx-background-color: #eef2ff; -fx-background-radius: 10; -fx-padding: 8;");
        dateBadge.setAlignment(Pos.CENTER);
        dateBadge.setPrefWidth(56);
        String[] dateParts = activity.getActivityDate().format(DateTimeFormatter.ofPattern("dd\nMMM")).split("\n");
        Label day = new Label(dateParts[0]);
        day.setStyle("-fx-font-size: 20px; -fx-font-weight: 800; -fx-text-fill: #6366f1;");
        Label month = new Label(dateParts[1].toUpperCase());
        month.setStyle("-fx-font-size: 10px; -fx-text-fill: #6b7280; -fx-font-weight: 700;");
        dateBadge.getChildren().addAll(day, month);

        // Project info
        VBox projInfo = new VBox(3);
        HBox.setHgrow(projInfo, Priority.ALWAYS);
        Label projName = new Label(projectMap.getOrDefault(activity.getProjectId(), "Unknown Project"));
        projName.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; -fx-text-fill: #111827;");

        // Status badge
        Label statusBadge = new Label(status);
        statusBadge.setStyle(getStatusStyle(status));

        projInfo.getChildren().addAll(projName, statusBadge);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Hours badge
        Label hoursLbl = new Label(String.format("%.1fh", activity.getHoursWorked()));
        hoursLbl.setStyle("-fx-background-color: #6366f1; -fx-text-fill: white; -fx-font-weight: 800; " +
                "-fx-padding: 6 14; -fx-background-radius: 20; -fx-font-size: 13px;");

        header.getChildren().addAll(dateBadge, projInfo, spacer, hoursLbl);

        // Description
        Label desc = new Label(activity.getDescription());
        desc.setStyle("-fx-font-size: 13px; -fx-text-fill: #4b5563;");
        desc.setWrapText(true);

        // Progress section
        VBox progressSection = new VBox(6);
        progressSection.setStyle("-fx-background-color: #f9fafb; -fx-padding: 12; -fx-background-radius: 10;");

        // Progress bar and percentage
        HBox progressHeader = new HBox(10);
        progressHeader.setAlignment(Pos.CENTER_LEFT);

        Label progressLabel = new Label("Progress");
        progressLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: #374151;");

        Region progressSpacer = new Region();
        HBox.setHgrow(progressSpacer, Priority.ALWAYS);

        Label percentLabel = new Label(completionPercent + "%");
        percentLabel.setStyle(getPercentStyle(completionPercent));

        progressHeader.getChildren().addAll(progressLabel, progressSpacer, percentLabel);

        // Progress bar
        ProgressBar progressBar = new ProgressBar(assignedHours > 0 ? Math.min(trackedHours / assignedHours, 1.0) : 0);
        progressBar.setPrefWidth(Double.MAX_VALUE);
        progressBar.setPrefHeight(8);
        progressBar.setStyle(getProgressBarStyle(completionPercent));

        // Time info
        HBox timeInfo = new HBox(15);
        timeInfo.setAlignment(Pos.CENTER_LEFT);

        Label trackedTime = new Label(String.format("⏱️ Tracked: %.1f / %.1f h", trackedHours, assignedHours));
        trackedTime.setStyle("-fx-font-size: 11px; -fx-text-fill: #6b7280;");

        if (activity.isTracking()) {
            Label activeLabel = new Label("● ACTIVE");
            activeLabel.setStyle("-fx-text-fill: #10b981; -fx-font-size: 11px; -fx-font-weight: bold;");
            timeInfo.getChildren().addAll(trackedTime, activeLabel);
        } else {
            timeInfo.getChildren().add(trackedTime);
        }

        progressSection.getChildren().addAll(progressHeader, progressBar, timeInfo);

        // Footer
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_LEFT);

        Label fullDate = new Label("📅 " + activity.getActivityDate().format(displayFormatter));
        fullDate.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af;");
        footer.getChildren().add(fullDate);

        LocalDate deadline = projectDeadlines.get(activity.getProjectId());
        if (deadline != null) {
            String deadlineStyle = deadline.isBefore(LocalDate.now()) && trackedHours < assignedHours
                    ? "#ef4444"
                    : "#6b7280";
            Label dl = new Label("⏰ Deadline: " + deadline.format(dateFormatter));
            dl.setStyle("-fx-font-size: 11px; -fx-text-fill: " + deadlineStyle + ";");
            footer.getChildren().add(dl);
        }

        // Add tracking sessions count with proper type handling
        try {
            Map<String, Object> summary = activityDAO.getActivityTrackingSummary(activity.getIdActivity());
            Object sessionsObj = summary.get("total_sessions");
            int sessionCount = 0;
            if (sessionsObj instanceof Long) {
                sessionCount = ((Long) sessionsObj).intValue();
            } else if (sessionsObj instanceof Integer) {
                sessionCount = (Integer) sessionsObj;
            }

            if (sessionCount > 0) {
                Label sessionsLabel = new Label("📊 " + sessionCount + " session(s)");
                sessionsLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af;");
                footer.getChildren().add(sessionsLabel);
            }
        } catch (Exception e) {
            // Ignore errors in session count
        }

        card.getChildren().addAll(header, new Separator(), desc, progressSection, footer);
        return card;
    }

    private String getStatusStyle(String status) {
        return switch (status) {
            case "In Progress" ->
                "-fx-background-color: #dbeafe; -fx-text-fill: #1e40af; -fx-padding: 3 10; -fx-background-radius: 10; -fx-font-size: 10px; -fx-font-weight: 700;";
            case "Completed" ->
                "-fx-background-color: #dcfce7; -fx-text-fill: #16a34a; -fx-padding: 3 10; -fx-background-radius: 10; -fx-font-size: 10px; -fx-font-weight: 700;";
            case "Overdue" ->
                "-fx-background-color: #fee2e2; -fx-text-fill: #ef4444; -fx-padding: 3 10; -fx-background-radius: 10; -fx-font-size: 10px; -fx-font-weight: 700;";
            case "Not Started" ->
                "-fx-background-color: #f3f4f6; -fx-text-fill: #6b7280; -fx-padding: 3 10; -fx-background-radius: 10; -fx-font-size: 10px; -fx-font-weight: 700;";
            default ->
                "-fx-background-color: #f3f4f6; -fx-text-fill: #6b7280; -fx-padding: 3 10; -fx-background-radius: 10; -fx-font-size: 10px; -fx-font-weight: 700;";
        };
    }

    private String getPercentStyle(int percent) {
        if (percent >= 100) {
            return "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #10b981;";
        } else if (percent >= 75) {
            return "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #f59e0b;";
        } else {
            return "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #ef4444;";
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

    private void updateStatistics() {
        if (filteredData == null)
            return;

        double totalAssignedHours = 0;
        long totalTrackedSeconds = 0;
        int completedActivities = 0;
        int inProgressActivities = 0;
        Set<Integer> uniqueProjects = new HashSet<>();

        for (Activity a : filteredData) {
            totalAssignedHours += a.getHoursWorked();
            Long tracked = trackedSecondsMap.get(a.getIdActivity());
            if (tracked != null) {
                totalTrackedSeconds += tracked;
            }
            uniqueProjects.add(a.getProjectId());

            String status = getActivityStatus(a);
            if (status.equals("Completed")) {
                completedActivities++;
            } else if (status.equals("In Progress")) {
                inProgressActivities++;
            }
        }

        double totalTrackedHours = totalTrackedSeconds / 3600.0;
        double avg = filteredData.isEmpty() ? 0 : totalAssignedHours / filteredData.size();
        double completionRate = totalAssignedHours > 0 ? (totalTrackedHours / totalAssignedHours) * 100 : 0;

        if (totalHoursLabel != null)
            totalHoursLabel.setText(String.format("%.1f", totalAssignedHours));
        if (totalActivitiesLabel != null)
            totalActivitiesLabel.setText(String.valueOf(filteredData.size()));
        if (projectsCountLabel != null)
            projectsCountLabel.setText(String.valueOf(uniqueProjects.size()));
        if (avgHoursLabel != null)
            avgHoursLabel.setText(String.format("%.1f", avg));
        if (totalTrackedLabel != null)
            totalTrackedLabel.setText(String.format("%.1f", totalTrackedHours));
        if (completionRateLabel != null)
            completionRateLabel.setText(String.format("%.1f%%", completionRate));
        if (overallProgressBar != null)
            overallProgressBar
                    .setProgress(totalAssignedHours > 0 ? Math.min(totalTrackedHours / totalAssignedHours, 1.0) : 0);
    }

    private void refreshData() {
        try {
            User user = AuthService.getCurrentUser();
            if (user != null) {
                List<Activity> activities = activityDAO.getByEmployeeId(user.getId());

                // Update tracked times
                for (Activity a : activities) {
                    Map<String, Object> summary = activityDAO.getActivityTrackingSummary(a.getIdActivity());
                    Object secondsObj = summary.get("total_seconds");
                    long totalSeconds = 0;
                    if (secondsObj instanceof Long) {
                        totalSeconds = (Long) secondsObj;
                    } else if (secondsObj instanceof Integer) {
                        totalSeconds = ((Integer) secondsObj).longValue();
                    }
                    trackedSecondsMap.put(a.getIdActivity(), totalSeconds);
                }

                masterData.setAll(activities);
                updateFilter(); // This will refresh the display
                System.out.println("Data refreshed at " + java.time.LocalTime.now());
            }
        } catch (Exception e) {
            System.err.println("Error refreshing data: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // === Sidebar Navigation ===
    @FXML
    private void handleDashboard() {
        stopAutoRefresh();
        SceneUtil.switchScene("dashboard.fxml");
    }

    @FXML
    private void handleJobOffers() {
        stopAutoRefresh();
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
        stopAutoRefresh();
        SceneUtil.switchScene("my_circle.fxml");
    }

    @FXML
    private void handleNotifications() {
        stopAutoRefresh();
        SceneUtil.switchScene("notifications.fxml");
    }

    @FXML
    private void handleActivities() {
        stopAutoRefresh();
        SceneUtil.switchScene("activities/activities.fxml");
    }

    @FXML
    private void handleProjects() {
        stopAutoRefresh();
        SceneUtil.switchScene("projects/projects.fxml");
    }

    @FXML
    private void handleToDo() {
        // Already here, just refresh
        refreshData();
    }

    @FXML
    private void handleMyProfile() {
        stopAutoRefresh();
        SceneUtil.switchScene("profile-view.fxml");
    }

    @FXML
    private void handleSettings() {
        stopAutoRefresh();
        SceneUtil.switchScene("settings.fxml");
    }

    @FXML
    private void handlePlaceholder() {
        new Alert(Alert.AlertType.INFORMATION, "Coming soon!").show();
    }

    @FXML
    private void handleLogout() {
        stopAutoRefresh();
        AuthService.logout();
        SceneUtil.switchScene("login.fxml");
    }
}