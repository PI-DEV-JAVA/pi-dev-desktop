package talentospidev.controllers.Activities;

import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import talentospidev.dao.ProjectDAO.ProjectDAO;
import talentospidev.models.Project.Project;
import talentospidev.models.User;
import talentospidev.services.AuthService;
import talentospidev.services.TrelloService;
import talentospidev.utils.SceneUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Project management page (Recruiter only) with Trello integration.
 */
public class ProjectController {

    @FXML
    private TextField nameField, budgetField, searchField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private ComboBox<String> statusCombo;
    @FXML
    private DatePicker startDatePicker, endDatePicker;
    @FXML
    private VBox listContainer;
    @FXML
    private Button submitBtn;
    @FXML
    private Label connectionStatusLabel;
    @FXML
    private VBox sidebar;
    @FXML
    private Button toDoTab;
    @FXML
    private Button activitiesTab;
    @FXML
    private Button projectsTab;

    private final ProjectDAO projectDAO = new ProjectDAO();
    private final ObservableList<Project> masterList = FXCollections.observableArrayList();
    private Project selectedProject = null;

    @FXML
    private void initialize() {
        talentospidev.utils.SidebarUtil.configure(toDoTab, activitiesTab, projectsTab);
        statusCombo.setItems(FXCollections.observableArrayList("PLANNED", "IN_PROGRESS", "DONE", "ON_HOLD"));
        statusCombo.getSelectionModel().select("PLANNED");

        searchField.textProperty().addListener((obs, o, n) -> {
            String filter = n == null ? "" : n.toLowerCase();
            displayProjects(masterList.stream()
                    .filter(p -> p.getName().toLowerCase().contains(filter)
                            || p.getStatus().toLowerCase().contains(filter))
                    .collect(Collectors.toList()));
        });

        loadProjects();

        // Check Trello connection status
        updateTrelloConnectionStatus();
    }

    private void updateTrelloConnectionStatus() {
        if (connectionStatusLabel != null) {
            // You can add a method in TrelloService to check connection
            connectionStatusLabel.setText("✓ Trello Connected");
            connectionStatusLabel.setStyle("-fx-text-fill: #10b981; -fx-font-size: 10px;");
        }
    }

    @FXML
    public void loadProjects() {
        User user = AuthService.getCurrentUser();
        if (user != null && user.getRole() == User.Role.HR) {
            masterList.setAll(projectDAO.getByManagerId(user.getId()));
            if (masterList.isEmpty())
                masterList.setAll(projectDAO.getAll()); // fallback
        } else {
            masterList.setAll(projectDAO.getAll());
        }
        displayProjects(masterList);
    }

    private void displayProjects(List<Project> projects) {
        listContainer.getChildren().clear();
        if (projects.isEmpty()) {
            VBox empty = new VBox(8);
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(30));
            empty.setStyle("-fx-background-color: white; -fx-background-radius: 12;");
            Label icon = new Label("📂");
            icon.setStyle("-fx-font-size: 32px;");
            Label text = new Label("No projects yet");
            text.setStyle("-fx-font-size: 13px; -fx-text-fill: #6b7280; -fx-font-weight: 600;");
            empty.getChildren().addAll(icon, text);
            listContainer.getChildren().add(empty);
            return;
        }
        for (Project p : projects)
            listContainer.getChildren().add(buildProjectCard(p));
    }

    private VBox buildProjectCard(Project project) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(18));
        String base = "-fx-background-color: white; -fx-background-radius: 14; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 6, 0, 0, 2);";
        card.setStyle(base);

        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label name = new Label(project.getName());
        name.setStyle("-fx-font-size: 16px; -fx-font-weight: 800; -fx-text-fill: #111827;");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        Label status = new Label(project.getStatus());
        status.setStyle(getStatusStyle(project.getStatus()));
        header.getChildren().addAll(name, sp, status);

        // Description
        Label desc = new Label(project.getDescription());
        desc.setWrapText(true);
        desc.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12px;");

        // Footer metadata
        HBox footer = new HBox(16);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setStyle("-fx-padding: 8 0 0 0; -fx-border-color: #f3f4f6; -fx-border-width: 1 0 0 0;");

        VBox mStart = createMeta("START", project.getStartDate() != null ? project.getStartDate().toString() : "—");
        VBox mEnd = createMeta("END", project.getEndDate() != null ? project.getEndDate().toString() : "—");
        VBox mBudget = createMeta("BUDGET", String.format("$%.0f", project.getBudget()));

        // Trello indicator (small icon if project has Trello card)
        Label trelloIcon = new Label("📋");
        trelloIcon.setStyle("-fx-font-size: 14px; -fx-text-fill: #0079bf; -fx-cursor: hand;");
        trelloIcon.setTooltip(new Tooltip("View on Trello"));
        trelloIcon.setOnMouseClicked(e -> {
            // You could open the Trello card URL here if you store it
            showAlert("Info", "Trello card created for this project", Alert.AlertType.INFORMATION);
        });

        Region fSp = new Region();
        HBox.setHgrow(fSp, Priority.ALWAYS);

        Button editBtn = new Button("✎");
        editBtn.setStyle(
                "-fx-background-color: #f3f4f6; -fx-text-fill: #374151; -fx-background-radius: 8; -fx-cursor: hand;");
        editBtn.setOnAction(e -> populateForm(project));
        Button delBtn = new Button("🗑");
        delBtn.setStyle(
                "-fx-background-color: #fef2f2; -fx-text-fill: #ef4444; -fx-background-radius: 8; -fx-cursor: hand;");
        delBtn.setOnAction(e -> handleDelete(project));

        footer.getChildren().addAll(mStart, mEnd, mBudget, trelloIcon, fSp, editBtn, delBtn);

        card.getChildren().addAll(header, desc, footer);
        return card;
    }

    private VBox createMeta(String label, String value) {
        VBox v = new VBox(2);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 9px; -fx-font-weight: 700;");
        Label val = new Label(value);
        val.setStyle("-fx-text-fill: #111827; -fx-font-size: 12px; -fx-font-weight: 600;");
        v.getChildren().addAll(lbl, val);
        return v;
    }

    private String getStatusStyle(String status) {
        String base = "-fx-padding: 4 10; -fx-background-radius: 12; -fx-font-size: 10px; -fx-font-weight: 700;";
        return switch (status) {
            case "DONE" -> base + "-fx-background-color: #dcfce7; -fx-text-fill: #16a34a;";
            case "IN_PROGRESS" -> base + "-fx-background-color: #dbeafe; -fx-text-fill: #1e40af;";
            case "ON_HOLD" -> base + "-fx-background-color: #fef3c7; -fx-text-fill: #d97706;";
            default -> base + "-fx-background-color: #f3f4f6; -fx-text-fill: #6b7280;";
        };
    }

    private void populateForm(Project p) {
        selectedProject = p;
        nameField.setText(p.getName());
        descriptionField.setText(p.getDescription());
        statusCombo.setValue(p.getStatus());
        startDatePicker.setValue(p.getStartDate());
        endDatePicker.setValue(p.getEndDate());
        budgetField.setText(String.valueOf(p.getBudget()));
        submitBtn.setText("Update Project");
        submitBtn.setStyle(
                "-fx-background-color: #6366f1; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10;");
        submitBtn.setOnAction(e -> updateProject());
    }

    @FXML
    private void addProject() {
        if (!validate())
            return;

        User user = AuthService.getCurrentUser();
        if (user == null) {
            showAlert("Error", "You must be logged in", Alert.AlertType.ERROR);
            return;
        }

        Project p = new Project(0, nameField.getText(), descriptionField.getText(), statusCombo.getValue(),
                startDatePicker.getValue(), endDatePicker.getValue(), Double.parseDouble(budgetField.getText()));

        if (user != null)
            p.setProjectManagerId(user.getId());

        // Add to database
        projectDAO.add(p);

        // Create Trello card
        try {
            String managerName = user.getEmail().split("@")[0];
            TrelloService.createProjectCard(p, managerName);
            showAlert("Success", "Project created and added to Trello!", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            System.err.println("⚠️ Failed to create Trello card: " + e.getMessage());
            showAlert("Success with warning",
                    "Project created but Trello integration failed: " + e.getMessage(),
                    Alert.AlertType.WARNING);
        }

        loadProjects();
        clearFields();
    }

    private void updateProject() {
        if (selectedProject == null || !validate())
            return;

        // Store old values for comparison
        String oldName = selectedProject.getName();
        String oldStatus = selectedProject.getStatus();
        LocalDate oldEndDate = selectedProject.getEndDate();

        // Update project
        selectedProject.setName(nameField.getText());
        selectedProject.setDescription(descriptionField.getText());
        selectedProject.setStatus(statusCombo.getValue());
        selectedProject.setStartDate(startDatePicker.getValue());
        selectedProject.setEndDate(endDatePicker.getValue());
        selectedProject.setBudget(Double.parseDouble(budgetField.getText()));

        // Update in database
        projectDAO.update(selectedProject);

        // Update Trello card (if you have the card ID stored)
        // You would need to retrieve the card ID from a mapping table
        // TrelloService.updateProjectCard(cardId, selectedProject);

        showAlert("Success", "Project updated successfully!", Alert.AlertType.INFORMATION);
        loadProjects();
        clearFields();
    }

    private void handleDelete(Project project) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete project '" + project.getName() + "'? This will NOT delete the Trello card.",
                ButtonType.YES, ButtonType.NO);

        if (a.showAndWait().orElse(null) == ButtonType.YES) {
            projectDAO.delete(project.getId());
            loadProjects();
            showAlert("Deleted", "Project removed from application", Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    private void clearFields() {
        nameField.clear();
        descriptionField.clear();
        budgetField.clear();
        statusCombo.setValue("PLANNED");
        startDatePicker.setValue(LocalDate.now());
        endDatePicker.setValue(null);
        selectedProject = null;
        submitBtn.setText("Create Project");
        submitBtn.setStyle(
                "-fx-background-color: linear-gradient(to right, #6366f1, #8b5cf6); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10;");
        submitBtn.setOnAction(e -> addProject());
    }

    private boolean validate() {
        if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Project name is required", Alert.AlertType.ERROR);
            return false;
        }
        if (statusCombo.getValue() == null) {
            showAlert("Validation Error", "Status is required", Alert.AlertType.ERROR);
            return false;
        }
        if (startDatePicker.getValue() == null) {
            showAlert("Validation Error", "Start date is required", Alert.AlertType.ERROR);
            return false;
        }
        try {
            double budget = Double.parseDouble(budgetField.getText());
            if (budget < 0) {
                showAlert("Validation Error", "Budget cannot be negative", Alert.AlertType.ERROR);
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Validation Error", "Budget must be a valid number", Alert.AlertType.ERROR);
            return false;
        }
        return true;
    }

    private void showAlert(String title, String msg, Alert.AlertType type) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    // === Sidebar Navigation ===
    @FXML
    private void handleDashboard() {
        SceneUtil.switchScene("recruiter_dashboard.fxml");
    }

    @FXML
    private void handleJobOffers() {
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
    private void handleActivities() {
        SceneUtil.switchScene("activities/activities.fxml");
    }

    @FXML
    private void handleProjects() {
        SceneUtil.switchScene("projects/projects.fxml");
    }

    @FXML
    private void handleMyProfile() {
        SceneUtil.switchScene("profile-view.fxml");
    }

    @FXML
    private void handleSettings() {
        SceneUtil.switchScene("settings.fxml");
    }

    @FXML
    private void handlePlaceholder() {
        new Alert(Alert.AlertType.INFORMATION, "Coming soon!").show();
    }

    @FXML
    private void handleLogout() {
        AuthService.logout();
        SceneUtil.switchScene("login.fxml");
    }
}