package talentospidev.controllers.Activities;
import talentospidev.services.TrelloService;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import talentospidev.dao.ActivityDAO.ActivityDAO;
import talentospidev.dao.ActivityDAO.ActivityFileDAO;
import talentospidev.dao.ProjectDAO.ProjectDAO;
import talentospidev.models.Activity.Activity;
import talentospidev.models.Project.Project;
import talentospidev.models.User;
import talentospidev.services.AuthService;
import talentospidev.services.EmailService;
import talentospidev.utils.DB;
import talentospidev.utils.SceneUtil;
import talentospidev.utils.ViewContext;

import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Recruiter Activities Manager — create/select projects, assign activities to
 * accepted candidates.
 */
public class ActivityController {

    @FXML
    private ComboBox<Employee> employeeCombo;
    @FXML
    private ComboBox<Project> projectCombo;
    @FXML
    private DatePicker datePicker;
    @FXML
    private TextArea descriptionField;
    @FXML
    private TextField hoursField, searchField;
    @FXML
    private VBox listContainer;
    @FXML
    private Button submitBtn;

    private final ActivityDAO activityDAO = new ActivityDAO();
    private final ActivityFileDAO fileDAO = new ActivityFileDAO();
    private final ProjectDAO projectDAO = new ProjectDAO();
    private final ObservableList<Activity> masterList = FXCollections.observableArrayList();
    private Activity selectedActivity = null;
    private final Map<Integer, String> userEmails = new HashMap<>();
    private final Map<Integer, String> userNames = new HashMap<>();
    private final Map<Integer, Integer> fileCountMap = new HashMap<>();

    // Small Employee wrapper for ComboBox
    public static class Employee {
        private final int id;
        private final String name;
        private final String email;

        public Employee(int id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }

        @Override
        public String toString() {
            return name + " (" + email + ")";
        }
    }

    @FXML
    private void initialize() {
        User user = AuthService.getCurrentUser();
        if (user == null || user.getRole() != User.Role.HR)
            return;

        loadAcceptedCandidates(user.getId());
        loadProjects(user.getId());
        loadAllActivities(user.getId());

        searchField.textProperty().addListener((obs, o, n) -> {
            String filter = n == null ? "" : n.toLowerCase();
            List<Activity> filtered = masterList.stream()
                    .filter(a -> {
                        String name = userNames.getOrDefault(a.getEmployeeId(), "").toLowerCase();
                        Project p = projectDAO.getById(a.getProjectId());
                        String projName = p != null ? p.getName().toLowerCase() : "";
                        return name.contains(filter) || projName.contains(filter)
                                || a.getDescription().toLowerCase().contains(filter);
                    }).collect(Collectors.toList());
            displayActivities(filtered);
        });
    }

    /**
     * Load only candidates that this recruiter has accepted (status contains
     * 'accept').
     */
    private void loadAcceptedCandidates(int recruiterId) {
        ObservableList<Employee> employees = FXCollections.observableArrayList();
        String sql = """
                    SELECT DISTINCT u.id, u.email, p.first_name, p.last_name
                    FROM users u
                    JOIN applications a ON a.user_id = u.id
                    JOIN offers o ON a.offer_id = o.id
                    LEFT JOIN profiles p ON p.user_id = u.id
                    WHERE o.recruiter_id = ? AND LOWER(a.status) LIKE '%accept%'
                """;
        try {
            Connection c = DB.getConnection();
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setInt(1, recruiterId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int uid = rs.getInt("id");
                String email = rs.getString("email");
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                String name = (firstName != null && lastName != null) ? firstName + " " + lastName
                        : email.split("@")[0];
                employees.add(new Employee(uid, name, email));
                userEmails.put(uid, email);
                userNames.put(uid, name);
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        employeeCombo.setItems(employees);
    }

    private void loadProjects(int recruiterId) {
        List<Project> projects = projectDAO.getByManagerId(recruiterId);
        if (projects.isEmpty())
            projects = projectDAO.getAll(); // fallback
        projectCombo.setItems(FXCollections.observableArrayList(projects));
    }

    @FXML
    public void loadAllActivities() {
        User user = AuthService.getCurrentUser();
        if (user != null)
            loadAllActivities(user.getId());
    }

    private void loadAllActivities(int recruiterId) {
        // Load activities for projects managed by this recruiter
        List<Project> myProjects = projectDAO.getByManagerId(recruiterId);
        masterList.clear();
        for (Project p : myProjects) {
            masterList.addAll(activityDAO.getByProjectId(p.getId()));
        }
        // If no projects yet, show all (fallback)
        if (myProjects.isEmpty())
            masterList.addAll(activityDAO.getAll());

        fileCountMap.clear();
        for (Activity a : masterList) {
            fileCountMap.put(a.getIdActivity(), fileDAO.getFilesByActivityId(a.getIdActivity()).size());
        }
        displayActivities(masterList);
    }

    private void displayActivities(List<Activity> activities) {
        listContainer.getChildren().clear();
        if (activities.isEmpty()) {
            VBox empty = new VBox(8);
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(30));
            empty.setStyle("-fx-background-color: white; -fx-background-radius: 12;");
            Label icon = new Label("📭");
            icon.setStyle("-fx-font-size: 32px;");
            Label text = new Label("No activities yet");
            text.setStyle("-fx-font-size: 14px; -fx-text-fill: #6b7280; -fx-font-weight: 600;");
            empty.getChildren().addAll(icon, text);
            listContainer.getChildren().add(empty);
            return;
        }
        for (Activity a : activities)
            listContainer.getChildren().add(buildActivityCard(a));
    }

    private HBox buildActivityCard(Activity activity) {
        HBox card = new HBox(16);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(16));
        String base = "-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 4, 0, 0, 1); -fx-cursor: hand;";
        String hover = "-fx-background-color: #fafbff; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(99,102,241,0.12), 6, 0, 0, 2); -fx-cursor: hand;";
        card.setStyle(base);
        card.setOnMouseEntered(e -> card.setStyle(hover));
        card.setOnMouseExited(e -> card.setStyle(base));

        // Click → navigate to admin details
        card.setOnMouseClicked(e -> {
            ViewContext.setSelectedActivityId(activity.getIdActivity());
            SceneUtil.switchScene("activities/admin_activity_details.fxml");
        });

        // Employee name
        VBox mainInfo = new VBox(3);
        mainInfo.setPrefWidth(160);
        String empName = userNames.getOrDefault(activity.getEmployeeId(), "User #" + activity.getEmployeeId());
        Label userLbl = new Label(empName.toUpperCase());
        userLbl.setStyle("-fx-font-weight: 800; -fx-font-size: 10px; -fx-text-fill: #94A3B8;");
        Project p = projectDAO.getById(activity.getProjectId());
        Label projLbl = new Label(p != null ? p.getName() : "General");
        projLbl.setStyle("-fx-text-fill: #111827; -fx-font-weight: 700; -fx-font-size: 14px;");
        mainInfo.getChildren().addAll(userLbl, projLbl);

        // Description
        VBox descBox = new VBox(3);
        HBox.setHgrow(descBox, Priority.ALWAYS);
        descBox.setStyle("-fx-background-color: #f9fafb; -fx-padding: 10; -fx-background-radius: 8;");
        Label descText = new Label(activity.getDescription());
        descText.setStyle("-fx-text-fill: #374151; -fx-font-size: 12px;");
        descText.setWrapText(true);
        descText.setMaxWidth(280);
        descBox.getChildren().add(descText);

        // Stats
        VBox stats = new VBox(2);
        stats.setAlignment(Pos.CENTER_RIGHT);
        stats.setMinWidth(60);
        Label hours = new Label(activity.getHours() + "h");
        hours.setStyle("-fx-font-weight: 800; -fx-font-size: 16px; -fx-text-fill: #111827;");
        Label date = new Label(activity.getDate().format(DateTimeFormatter.ofPattern("dd MMM")));
        date.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 10px; -fx-font-weight: 600;");
        stats.getChildren().addAll(hours, date);

        // File count
        int fc = fileCountMap.getOrDefault(activity.getIdActivity(), 0);
        if (fc > 0) {
            Label fileIcon = new Label("📎 " + fc);
            fileIcon.setStyle("-fx-font-size: 11px; -fx-text-fill: #6b7280;");
            stats.getChildren().add(fileIcon);
        }

        // Actions
        VBox actions = new VBox(6);
        actions.setAlignment(Pos.CENTER);
        Button editBtn = new Button("✎");
        editBtn.setStyle(
                "-fx-background-color: #f3f4f6; -fx-text-fill: #374151; -fx-background-radius: 8; -fx-cursor: hand;");
        editBtn.setOnAction(e -> {
            e.consume();
            populateForm(activity);
        });
        Button delBtn = new Button("🗑");
        delBtn.setStyle(
                "-fx-background-color: #fef2f2; -fx-text-fill: #ef4444; -fx-background-radius: 8; -fx-cursor: hand;");
        delBtn.setOnAction(e -> {
            e.consume();
            handleDelete(activity);
        });
        actions.getChildren().addAll(editBtn, delBtn);

        card.getChildren().addAll(mainInfo, descBox, stats, actions);
        return card;
    }

    private void populateForm(Activity activity) {
        selectedActivity = activity;
        String email = userEmails.get(activity.getEmployeeId());
        String name = userNames.getOrDefault(activity.getEmployeeId(), "");
        employeeCombo.setValue(new Employee(activity.getEmployeeId(), name, email != null ? email : ""));
        projectCombo.setValue(projectDAO.getById(activity.getProjectId()));
        datePicker.setValue(activity.getDate());
        descriptionField.setText(activity.getDescription());
        hoursField.setText(String.valueOf(activity.getHoursWorked()));
        submitBtn.setText("Update");
        submitBtn.setStyle(
                "-fx-background-color: #6366f1; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10;");
        submitBtn.setOnAction(e -> updateActivity());
    }

    @FXML
    private void addActivity() {
        if (!validate())
            return;
        
        Employee emp = employeeCombo.getValue();
        Project proj = projectCombo.getValue();
        
        Activity a = new Activity(emp.getId(), proj.getId(), datePicker.getValue(),
                descriptionField.getText(), Double.parseDouble(hoursField.getText()));
        
        // Add activity to database
        activityDAO.add(a);
        
        // Send email notification
        try {
            String formattedDate = a.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            EmailService.sendActivityAssignmentEmail(
                emp.getEmail(),
                emp.getName(),
                proj.getName(),
                a.getDescription(),
                a.getHoursWorked(),
                formattedDate
            );
            showAlert("Success", "Activity assigned to " + emp.getName() + "! An email notification has been sent.", 
                     Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            showAlert("Success with warning", "Activity assigned but email notification failed: " + e.getMessage(), 
                     Alert.AlertType.WARNING);
        }
        
        // Create Trello card
        try {
            TrelloService.createActivityCard(a, emp.getName(), proj.getName());
            System.out.println("✅ Trello card created for activity");
        } catch (Exception e) {
            System.err.println("⚠️ Failed to create Trello card: " + e.getMessage());
            // Don't show alert to user, just log it
        }
        
        loadAllActivities();
        clear();
    }

    private void updateActivity() {
        if (selectedActivity == null || !validate())
            return;
        
        Employee emp = employeeCombo.getValue();
        Project proj = projectCombo.getValue();
        
        // Store old values for comparison
        int oldEmployeeId = selectedActivity.getEmployeeId();
        double oldHours = selectedActivity.getHoursWorked();
        String oldDescription = selectedActivity.getDescription();
        
        // Update activity
        selectedActivity.setEmployeeId(emp.getId());
        selectedActivity.setProjectId(proj.getId());
        selectedActivity.setDate(datePicker.getValue());
        selectedActivity.setDescription(descriptionField.getText());
        selectedActivity.setHoursWorked(Double.parseDouble(hoursField.getText()));
        
        activityDAO.update(selectedActivity);
        
        // Send email notification
        try {
            String formattedDate = selectedActivity.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            
            if (oldEmployeeId != emp.getId()) {
                // Activity reassigned to different employee
                EmailService.sendActivityAssignmentEmail(
                    emp.getEmail(),
                    emp.getName(),
                    proj.getName(),
                    selectedActivity.getDescription(),
                    selectedActivity.getHoursWorked(),
                    formattedDate
                );
                showAlert("Success", "Activity updated and reassigned! Email notification sent to new employee.", 
                         Alert.AlertType.INFORMATION);
            } else {
                // Activity updated for same employee
                StringBuilder changes = new StringBuilder();
                if (!oldDescription.equals(selectedActivity.getDescription())) {
                    changes.append("• Description was updated\n");
                }
                if (oldHours != selectedActivity.getHoursWorked()) {
                    changes.append(String.format("• Hours changed from %.1f to %.1f\n", oldHours, selectedActivity.getHoursWorked()));
                }
                
                if (changes.length() > 0) {
                    EmailService.sendActivityUpdateEmail(
                        emp.getEmail(),
                        emp.getName(),
                        proj.getName(),
                        selectedActivity.getDescription(),
                        selectedActivity.getHoursWorked(),
                        formattedDate,
                        changes.toString()
                    );
                    showAlert("Success", "Activity updated! Notification sent to employee.", 
                             Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Success", "Activity updated! (No changes detected, email not sent)", 
                             Alert.AlertType.INFORMATION);
                }
            }
        } catch (Exception e) {
            showAlert("Success with warning", "Activity updated but email notification failed: " + e.getMessage(), 
                     Alert.AlertType.WARNING);
        }
        
        loadAllActivities();
        clear();
    }

    private void handleDelete(Activity activity) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Delete this activity?", ButtonType.YES, ButtonType.NO);
        if (a.showAndWait().orElse(null) == ButtonType.YES) {
            activityDAO.delete(activity.getIdActivity());
            loadAllActivities();
        }
    }

    @FXML
    private void clear() {
        employeeCombo.setValue(null);
        projectCombo.setValue(null);
        datePicker.setValue(null);
        descriptionField.clear();
        hoursField.clear();
        selectedActivity = null;
        submitBtn.setText("Log Activity");
        submitBtn.setStyle(
                "-fx-background-color: linear-gradient(to right, #6366f1, #8b5cf6); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10;");
        submitBtn.setOnAction(e -> addActivity());
    }

    private boolean validate() {
        try {
            return employeeCombo.getValue() != null && projectCombo.getValue() != null
                    && datePicker.getValue() != null && Double.parseDouble(hoursField.getText()) > 0;
        } catch (Exception e) {
            return false;
        }
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
    private void handlePlaceholder() {
        new Alert(Alert.AlertType.INFORMATION, "Coming soon!").show();
    }

    @FXML
    private void handleLogout() {
        AuthService.logout();
        SceneUtil.switchScene("login.fxml");
    }
}