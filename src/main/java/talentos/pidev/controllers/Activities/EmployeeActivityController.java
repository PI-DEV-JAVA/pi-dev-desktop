package talentos.pidev.controllers.Activities;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import talentos.pidev.models.dao.ActivityDAO.ActivityDAO;
import talentos.pidev.models.dao.ProjectDAO.ProjectDAO;
import talentos.pidev.models.schema.Activity.Activity;
import talentos.pidev.models.schema.Project.Project;
import talentos.pidev.utils.DB;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class EmployeeActivityController {

    @FXML private Label welcomeLabel;
    @FXML private Label totalHoursLabel;
    @FXML private Label totalActivitiesLabel;
    @FXML private Label projectsCountLabel;
    @FXML private Label avgHoursLabel;
    @FXML private Label activitiesCountLabel;
    
    // Filter components
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterProjectCombo;
    @FXML private DatePicker filterDatePicker;
    @FXML private Button clearFilterBtn;
    
    // Activities container
    @FXML private VBox activitiesContainer;

    private final ActivityDAO activityDAO = new ActivityDAO();
    private final ProjectDAO projectDAO = new ProjectDAO();
    
    private int currentEmployeeId;
    private String currentEmployeeName;
    private ObservableList<Activity> masterData = FXCollections.observableArrayList();
    private FilteredList<Activity> filteredData;
    private Map<Integer, String> projectMap = new HashMap<>();
    private Map<Integer, String> projectStatusMap = new HashMap<>();
    private Map<Integer, LocalDate> projectDeadlines = new HashMap<>();
    
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private final DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy");

    @FXML
    private void initialize() {
        loadLoggedInUser();
        loadProjectsData();
        setupFilters();
        loadUserActivities();
        
        clearFilterBtn.setOnAction(e -> clearFilters());
    }

    private void loadLoggedInUser() {
        // TODO: Replace with your actual authentication logic
        currentEmployeeId = getLoggedInEmployeeId();
        currentEmployeeName = getLoggedInEmployeeName();
        
        welcomeLabel.setText("Welcome back, " + currentEmployeeName + "!");
    }

    private int getLoggedInEmployeeId() {
        // Try to get the current employee ID from the activities table
        try (Connection conn = DB.getConnection()) {
            // You might want to store the logged-in user ID in a session variable
            // For now, let's try to get the first employee ID from activities
            String sql = "SELECT DISTINCT employee_id FROM activities LIMIT 1";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return rs.getInt("employee_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1; // Default fallback
    }

    private String getLoggedInEmployeeName() {
        // Since we don't have a users table, return a generic name with ID
        return "Employee #" + currentEmployeeId;
    }

    private void loadProjectsData() {
        List<Project> projects = projectDAO.getAll();
        
        projectMap.clear();
        projectStatusMap.clear();
        projectDeadlines.clear();
        
        // Add "All Projects" option
        ObservableList<String> projectNames = FXCollections.observableArrayList();
        projectNames.add("All Projects");
        
        for (Project project : projects) {
            projectMap.put(project.getId(), project.getName());
            projectStatusMap.put(project.getId(), project.getStatus());
            projectDeadlines.put(project.getId(), project.getEndDate());
            projectNames.add(project.getName());
        }
        
        filterProjectCombo.setItems(projectNames);
        filterProjectCombo.getSelectionModel().selectFirst();
    }

    private void loadUserActivities() {
        // Load activities for the current employee
        List<Activity> activities = activityDAO.getByEmployeeId(currentEmployeeId);
        masterData.setAll(activities);
        
        // Setup filtered data
        filteredData = new FilteredList<>(masterData, p -> true);
        
        // Display activities
        displayActivities();
        updateStatistics();
    }

    private void setupFilters() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> updateFilter());
        filterProjectCombo.valueProperty().addListener((observable, oldValue, newValue) -> updateFilter());
        filterDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> updateFilter());
    }

    private void updateFilter() {
        if (filteredData == null) return;
        
        filteredData.setPredicate(activity -> {
            // Search by description
            if (searchField.getText() != null && !searchField.getText().isEmpty()) {
                String searchLower = searchField.getText().toLowerCase();
                if (!activity.getDescription().toLowerCase().contains(searchLower)) {
                    return false;
                }
            }
            
            // Filter by project
            String selectedProject = filterProjectCombo.getValue();
            if (selectedProject != null && !selectedProject.equals("All Projects")) {
                String projectName = projectMap.get(activity.getProjectId());
                if (!selectedProject.equals(projectName)) {
                    return false;
                }
            }
            
            // Filter by date
            LocalDate filterDate = filterDatePicker.getValue();
            if (filterDate != null) {
                if (!activity.getActivityDate().equals(filterDate)) {
                    return false;
                }
            }
            
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
            // Show empty state
            VBox emptyState = createEmptyState();
            activitiesContainer.getChildren().add(emptyState);
            activitiesCountLabel.setText("No activities found");
            return;
        }
        
        // Sort activities by date (most recent first)
        List<Activity> sortedActivities = new ArrayList<>(filteredData);
        sortedActivities.sort((a1, a2) -> a2.getActivityDate().compareTo(a1.getActivityDate()));
        
        for (Activity activity : sortedActivities) {
            VBox activityCard = createActivityCard(activity);
            activitiesContainer.getChildren().add(activityCard);
        }
        
        activitiesCountLabel.setText("Showing " + filteredData.size() + " activity(ies)");
    }

    private VBox createActivityCard(Activity activity) {
        VBox card = new VBox(15);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 20; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 5);");
        
        // Make card clickable
        card.setOnMouseClicked(e -> openActivityDetails(activity));
        card.setCursor(javafx.scene.Cursor.HAND);
        
        // Add hover effect
        card.setOnMouseEntered(e -> 
            card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 20; " +
                         "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 5); -fx-scale-x: 1.02; -fx-scale-y: 1.02;")
        );
        card.setOnMouseExited(e -> 
            card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 20; " +
                         "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 5); -fx-scale-x: 1; -fx-scale-y: 1;")
        );
        // Header with date and project
        HBox header = new HBox(10);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        // Date badge
        VBox dateBadge = new VBox(5);
        dateBadge.setStyle("-fx-background-color: #F1F5F9; -fx-background-radius: 10; -fx-padding: 10;");
        dateBadge.setAlignment(javafx.geometry.Pos.CENTER);
        dateBadge.setPrefWidth(70);
        dateBadge.setPrefHeight(70);
        
        String[] dateParts = activity.getActivityDate().format(DateTimeFormatter.ofPattern("dd\nMMM")).split("\n");
        Label dayLabel = new Label(dateParts[0]);
        dayLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #0D203B;");
        Label monthLabel = new Label(dateParts[1].toUpperCase());
        monthLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #718096;");
        dateBadge.getChildren().addAll(dayLabel, monthLabel);
        
        // Project info
        VBox projectInfo = new VBox(5);
        projectInfo.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label projectNameLabel = new Label(projectMap.getOrDefault(activity.getProjectId(), "Unknown Project"));
        projectNameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0D203B;");
        
        // Status badge
        String status = projectStatusMap.getOrDefault(activity.getProjectId(), "UNKNOWN");
        Label statusLabel = new Label(status);
        statusLabel.setStyle(getStatusStyle(status));
        
        HBox statusBox = new HBox(statusLabel);
        statusBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        projectInfo.getChildren().addAll(projectNameLabel, statusBox);
        
        // Create spacer using Region with HBox.setHgrow
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Hours badge
        Label hoursLabel = new Label(String.format("%.1f h", activity.getHoursWorked()));
        hoursLabel.setStyle("-fx-background-color: #0D203B; -fx-text-fill: white; -fx-font-weight: bold; " +
                           "-fx-padding: 8 15; -fx-background-radius: 20; -fx-font-size: 14px;");
        
        header.getChildren().addAll(dateBadge, projectInfo, spacer, hoursLabel);
        
        // Description
        Label descriptionLabel = new Label(activity.getDescription());
        descriptionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #4A5568; -fx-wrap-text: true;");
        descriptionLabel.setWrapText(true);
        
        // Separator
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #E2E8F0;");
        
        // Footer with metadata
        HBox footer = new HBox(15);
        footer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        // Full date
        Label fullDateLabel = new Label("📅 " + activity.getActivityDate().format(displayFormatter));
        fullDateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #718096;");
        footer.getChildren().add(fullDateLabel);
        
        // Project deadline if available
        LocalDate deadline = projectDeadlines.get(activity.getProjectId());
        if (deadline != null) {
            Label deadlineLabel = new Label("⏰ Deadline: " + deadline.format(dateFormatter));
            deadlineLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + 
                                  (deadline.isBefore(LocalDate.now()) ? "#E53E3E" : "#718096") + ";");
            footer.getChildren().add(deadlineLabel);
        }
        
        card.getChildren().addAll(header, separator, descriptionLabel, footer);
        
        return card;
    }

    private String getStatusStyle(String status) {
        switch (status) {
            case "PLANNED":
                return "-fx-background-color: #CBD5E0; -fx-text-fill: #2D3748; -fx-padding: 4 10; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;";
            case "IN_PROGRESS":
                return "-fx-background-color: #3182CE; -fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;";
            case "DONE":
                return "-fx-background-color: #38A169; -fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;";
            case "ON_HOLD":
                return "-fx-background-color: #ECC94B; -fx-text-fill: #744210; -fx-padding: 4 10; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;";
            default:
                return "-fx-background-color: #CBD5E0; -fx-text-fill: #2D3748; -fx-padding: 4 10; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;";
        }
    }

    private VBox createEmptyState() {
        VBox emptyState = new VBox(20);
        emptyState.setAlignment(javafx.geometry.Pos.CENTER);
        emptyState.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 50;");
        emptyState.setPrefHeight(300);
        
        Label iconLabel = new Label("📭");
        iconLabel.setStyle("-fx-font-size: 64px;");
        
        Label titleLabel = new Label("No Activities Found");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #0D203B;");
        
        Label descLabel = new Label("No activities match your current filters.\nTry adjusting your search criteria.");
        descLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #718096; -fx-text-alignment: center;");
        descLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        
        Button clearFiltersBtn = new Button("Clear All Filters");
        clearFiltersBtn.setStyle("-fx-background-color: #0D203B; -fx-text-fill: white; -fx-font-weight: bold; " +
                                "-fx-padding: 10 25; -fx-background-radius: 25; -fx-cursor: hand;");
        clearFiltersBtn.setOnAction(e -> clearFilters());
        
        emptyState.getChildren().addAll(iconLabel, titleLabel, descLabel, clearFiltersBtn);
        
        return emptyState;
    }

    private void updateStatistics() {
        if (filteredData == null) return;
        
        double totalHours = 0;
        Set<Integer> uniqueProjects = new HashSet<>();
        
        for (Activity activity : filteredData) {
            totalHours += activity.getHoursWorked();
            uniqueProjects.add(activity.getProjectId());
        }
        
        double avgHours = filteredData.isEmpty() ? 0 : totalHours / filteredData.size();
        
        totalHoursLabel.setText(String.format("%.1f", totalHours));
        totalActivitiesLabel.setText(String.valueOf(filteredData.size()));
        projectsCountLabel.setText(String.valueOf(uniqueProjects.size()));
        avgHoursLabel.setText(String.format("%.1f", avgHours));
    }

    // Public method to set employee ID from login
    public void setCurrentEmployeeId(int employeeId) {
        this.currentEmployeeId = employeeId;
        Platform.runLater(() -> {
            loadLoggedInUser();
            loadUserActivities();
        });
    }
    private void openActivityDetails(Activity activity) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/activity_details.fxml"));
            Parent root = loader.load();
            
            ActivityDetailsController controller = loader.getController();
            controller.setActivity(activity);
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(new Scene(root));
            stage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open activity details: " + e.getMessage());
        }
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}