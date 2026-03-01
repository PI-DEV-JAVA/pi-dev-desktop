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
    private TextField searchField;
    @FXML
    private ComboBox<String> filterProjectCombo;
    @FXML
    private DatePicker filterDatePicker;
    @FXML
    private Button clearFilterBtn;
    @FXML
    private VBox activitiesContainer;

    private final ActivityDAO activityDAO = new ActivityDAO();
    private final ProjectDAO projectDAO = new ProjectDAO();
    private ObservableList<Activity> masterData = FXCollections.observableArrayList();
    private FilteredList<Activity> filteredData;
    private final Map<Integer, String> projectMap = new HashMap<>();
    private final Map<Integer, String> projectStatusMap = new HashMap<>();
    private final Map<Integer, LocalDate> projectDeadlines = new HashMap<>();

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private final DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy");
    @FXML
    private VBox sidebar;

    @FXML
    private void initialize() {
        talentospidev.utils.SidebarUtil.applySidebarIcons(sidebar);
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
    }

    private void loadProjectsData() {
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
        filterProjectCombo.setItems(projectNames);
        filterProjectCombo.getSelectionModel().selectFirst();
    }

    private void loadUserActivities(int userId) {
        List<Activity> activities = activityDAO.getByEmployeeId(userId);
        masterData.setAll(activities);
        filteredData = new FilteredList<>(masterData, p -> true);
        displayActivities();
        updateStatistics();
    }

    private void setupFilters() {
        searchField.textProperty().addListener((o, ov, nv) -> updateFilter());
        filterProjectCombo.valueProperty().addListener((o, ov, nv) -> updateFilter());
        filterDatePicker.valueProperty().addListener((o, ov, nv) -> updateFilter());
    }

    private void updateFilter() {
        if (filteredData == null)
            return;
        filteredData.setPredicate(activity -> {
            if (searchField.getText() != null && !searchField.getText().isEmpty()) {
                if (!activity.getDescription().toLowerCase().contains(searchField.getText().toLowerCase()))
                    return false;
            }
            String sel = filterProjectCombo.getValue();
            if (sel != null && !sel.equals("All Projects")) {
                String pn = projectMap.get(activity.getProjectId());
                if (!sel.equals(pn))
                    return false;
            }
            LocalDate fd = filterDatePicker.getValue();
            if (fd != null && !activity.getActivityDate().equals(fd))
                return false;
            return true;
        });
        displayActivities();
        updateStatistics();
    }

    private void clearFilters() {
        searchField.clear();
        filterProjectCombo.getSelectionModel().selectFirst();
        filterDatePicker.setValue(null);
    }

    private void displayActivities() {
        activitiesContainer.getChildren().clear();
        if (filteredData == null || filteredData.isEmpty()) {
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
            return;
        }

        List<Activity> sorted = new ArrayList<>(filteredData);
        sorted.sort((a, b) -> b.getActivityDate().compareTo(a.getActivityDate()));
        for (Activity a : sorted)
            activitiesContainer.getChildren().add(createActivityCard(a));
        if (activitiesCountLabel != null)
            activitiesCountLabel.setText("Showing " + filteredData.size() + " activity(ies)");
    }

    private VBox createActivityCard(Activity activity) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(18));
        String base = "-fx-background-color: white; -fx-background-radius: 14; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 6, 0, 0, 2); -fx-cursor: hand;";
        String hover = "-fx-background-color: #fafbff; -fx-background-radius: 14; " +
                "-fx-effect: dropshadow(gaussian, rgba(99,102,241,0.12), 8, 0, 0, 3); -fx-cursor: hand;";
        card.setStyle(base);
        card.setOnMouseEntered(e -> card.setStyle(hover));
        card.setOnMouseExited(e -> card.setStyle(base));

        // Click → navigate to activity details
        card.setOnMouseClicked(e -> {
            ViewContext.setSelectedActivityId(activity.getIdActivity());
            SceneUtil.switchScene("activities/activity_details.fxml");
        });

        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

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

        VBox projInfo = new VBox(3);
        HBox.setHgrow(projInfo, Priority.ALWAYS);
        Label projName = new Label(projectMap.getOrDefault(activity.getProjectId(), "Unknown Project"));
        projName.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; -fx-text-fill: #111827;");
        String status = projectStatusMap.getOrDefault(activity.getProjectId(), "PLANNED");
        Label statusBadge = new Label(status);
        statusBadge.setStyle(getStatusStyle(status));
        projInfo.getChildren().addAll(projName, statusBadge);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label hoursLbl = new Label(String.format("%.1fh", activity.getHoursWorked()));
        hoursLbl.setStyle("-fx-background-color: #6366f1; -fx-text-fill: white; -fx-font-weight: 800; " +
                "-fx-padding: 6 14; -fx-background-radius: 20; -fx-font-size: 13px;");

        header.getChildren().addAll(dateBadge, projInfo, spacer, hoursLbl);

        // Description
        Label desc = new Label(activity.getDescription());
        desc.setStyle("-fx-font-size: 13px; -fx-text-fill: #4b5563;");
        desc.setWrapText(true);

        // Footer
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_LEFT);
        Label fullDate = new Label("📅 " + activity.getActivityDate().format(displayFormatter));
        fullDate.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af;");
        footer.getChildren().add(fullDate);

        LocalDate deadline = projectDeadlines.get(activity.getProjectId());
        if (deadline != null) {
            Label dl = new Label("⏰ Deadline: " + deadline.format(dateFormatter));
            dl.setStyle("-fx-font-size: 11px; -fx-text-fill: " +
                    (deadline.isBefore(LocalDate.now()) ? "#ef4444" : "#6b7280") + ";");
            footer.getChildren().add(dl);
        }

        card.getChildren().addAll(header, new Separator(), desc, footer);
        return card;
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

    private void updateStatistics() {
        if (filteredData == null)
            return;
        double totalHours = 0;
        Set<Integer> uniqueProjects = new HashSet<>();
        for (Activity a : filteredData) {
            totalHours += a.getHoursWorked();
            uniqueProjects.add(a.getProjectId());
        }
        double avg = filteredData.isEmpty() ? 0 : totalHours / filteredData.size();
        if (totalHoursLabel != null)
            totalHoursLabel.setText(String.format("%.1f", totalHours));
        if (totalActivitiesLabel != null)
            totalActivitiesLabel.setText(String.valueOf(filteredData.size()));
        if (projectsCountLabel != null)
            projectsCountLabel.setText(String.valueOf(uniqueProjects.size()));
        if (avgHoursLabel != null)
            avgHoursLabel.setText(String.format("%.1f", avg));
    }

    // === Sidebar Navigation ===
    @FXML
    private void handleDashboard() {
        SceneUtil.switchScene("dashboard.fxml");
    }

    @FXML
    private void handleJobOffers() {
        SceneUtil.switchScene("OffersCardView.fxml");
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
    private void handleToDo() {
        SceneUtil.switchScene("activities/activity_employee.fxml");
    }

    @FXML
    private void handleMyProfile() {
        SceneUtil.switchScene("profile-view.fxml");
    }

    @FXML
    private void handlePlaceholder() {
        new Alert(Alert.AlertType.INFORMATION, "Coming soon!").show();
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