package pi_dev.controller;

import javafx.collections.*;
import javafx.collections.transformation.FilteredList;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import pi_dev.dao.ActivityDAO;
import pi_dev.dao.ProjectDAO;
import pi_dev.model.Activity;
import pi_dev.model.Project;
import pi_dev.MainApp;
import pi_dev.config.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivityController {

    @FXML private ComboBox<Integer> employeeCombo;
    @FXML private ComboBox<Project> projectCombo;
    @FXML private DatePicker datePicker;
    @FXML private TextArea descriptionField;
    @FXML private TextField hoursField;
    @FXML private TextField searchField;

    @FXML private TableView<Activity> table;
    @FXML private TableColumn<Activity, Integer> colEmployee;
    @FXML private TableColumn<Activity, String> colProject;
    @FXML private TableColumn<Activity, LocalDate> colDate;
    @FXML private TableColumn<Activity, String> colDescription;
    @FXML private TableColumn<Activity, Integer> colHours;

    private final ActivityDAO activityDAO = new ActivityDAO();
    private final ProjectDAO projectDAO = new ProjectDAO();
    private final ObservableList<Activity> masterList = FXCollections.observableArrayList();
    private FilteredList<Activity> filteredList;

    private Activity selectedActivity;
    private final Map<Integer, String> userEmails = new HashMap<>();

    @FXML
    private void initialize() {
        // Set up table columns
        colEmployee.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colHours.setCellValueFactory(new PropertyValueFactory<>("hours"));
        
        // Custom cell value factory for project column (show project name)
        colProject.setCellValueFactory(cellData -> {
            Activity activity = cellData.getValue();
            if (activity != null) {
                Project project = getProjectById(activity.getProjectId());
                return new SimpleStringProperty(project != null ? project.getName() : "Unknown");
            }
            return new SimpleStringProperty("");
        });

        // Initialize filtered list
        filteredList = new FilteredList<>(masterList, p -> true);
        table.setItems(filteredList);

        // Search functionality
        searchField.textProperty().addListener((obs, oldValue, newValue) ->
            filteredList.setPredicate(activity -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                
                String lowerCaseFilter = newValue.toLowerCase();
                
                // Search by employee ID
                if (String.valueOf(activity.getEmployeeId()).contains(lowerCaseFilter)) {
                    return true;
                }
                
                // Search by description
                if (activity.getDescription().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                
                // Search by project name
                Project project = getProjectById(activity.getProjectId());
                if (project != null && project.getName().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                
                return false;
            })
        );

        // Table selection listener
        table.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> {
                selectedActivity = newVal;
                if (newVal != null) {
                    populateForm(newVal);
                }
            }
        );

        // Load initial data
        loadUsers();
        loadProjects();
        loadAllActivities();
    }

    private void loadUsers() {
        ObservableList<Integer> ids = FXCollections.observableArrayList();
        String sql = "SELECT id_user, email FROM users";

        try (Connection c = DBConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                int userId = rs.getInt("id_user");
                String email = rs.getString("email");
                ids.add(userId);
                userEmails.put(userId, email);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load users: " + e.getMessage(), Alert.AlertType.ERROR);
        }

        employeeCombo.setItems(ids);
        employeeCombo.setCellFactory(cb -> new ListCell<Integer>() {
            @Override
            protected void updateItem(Integer id, boolean empty) {
                super.updateItem(id, empty);
                if (empty || id == null) {
                    setText(null);
                } else {
                    String email = userEmails.get(id);
                    setText(email != null ? email : "User #" + id);
                }
            }
        });
        
        employeeCombo.setButtonCell(new ListCell<Integer>() {
            @Override
            protected void updateItem(Integer id, boolean empty) {
                super.updateItem(id, empty);
                if (empty || id == null) {
                    setText(null);
                } else {
                    String email = userEmails.get(id);
                    setText(email != null ? email : "User #" + id);
                }
            }
        });
    }

    private void loadProjects() {
        List<Project> projects = projectDAO.getAll();
        ObservableList<Project> projectList = FXCollections.observableArrayList(projects);
        projectCombo.setItems(projectList);
        
        // Set cell factory to show project names
        projectCombo.setCellFactory(cb -> new ListCell<Project>() {
            @Override
            protected void updateItem(Project project, boolean empty) {
                super.updateItem(project, empty);
                setText(empty || project == null ? null : project.getName());
            }
        });
        
        projectCombo.setButtonCell(new ListCell<Project>() {
            @Override
            protected void updateItem(Project project, boolean empty) {
                super.updateItem(project, empty);
                setText(empty || project == null ? null : project.getName());
            }
        });
        
        // Listener for project selection
        projectCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadActivitiesByProject(newVal.getId());
            }
        });
    }

    private void loadAllActivities() {
        List<Activity> activities = activityDAO.getAll();
        masterList.setAll(activities);
    }

    private void loadActivitiesByProject(int projectId) {
        List<Activity> activities = activityDAO.getByProjectId(projectId);
        masterList.setAll(activities);
    }

    private Project getProjectById(int projectId) {
        List<Project> projects = projectDAO.getAll();
        return projects.stream()
                .filter(p -> p.getId() == projectId)
                .findFirst()
                .orElse(null);
    }

    private void populateForm(Activity activity) {
        if (activity != null) {
            employeeCombo.setValue(activity.getEmployeeId());
            
            // Find and set the project
            Project project = getProjectById(activity.getProjectId());
            if (project != null) {
                projectCombo.setValue(project);
            }
            
            datePicker.setValue(activity.getDate());
            descriptionField.setText(activity.getDescription());
            hoursField.setText(String.valueOf(activity.getHours()));
        }
    }

    @FXML
    private void addActivity() {
        if (!validate()) {
            showAlert("Validation Error", "Please fill all fields correctly", Alert.AlertType.WARNING);
            return;
        }

        try {
            // FIXED: Correct constructor call based on your Activity model
            Activity activity = new Activity(
                employeeCombo.getValue(),
                projectCombo.getValue().getId(),  // projectId parameter
                datePicker.getValue(),
                descriptionField.getText(),
                Integer.parseInt(hoursField.getText())
            );

            activityDAO.add(activity);
            loadAllActivities();  // Reload all activities
            clear();
            showAlert("Success", "Activity added successfully", Alert.AlertType.INFORMATION);
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to add activity: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void updateActivity() {
        if (selectedActivity == null) {
            showAlert("No Selection", "Please select an activity to update", Alert.AlertType.WARNING);
            return;
        }

        if (!validate()) {
            showAlert("Validation Error", "Please fill all fields correctly", Alert.AlertType.WARNING);
            return;
        }

        try {
            selectedActivity.setEmployeeId(employeeCombo.getValue());
            selectedActivity.setProjectId(projectCombo.getValue().getId());
            selectedActivity.setDate(datePicker.getValue());
            selectedActivity.setDescription(descriptionField.getText());
            selectedActivity.setHours(Integer.parseInt(hoursField.getText()));

            activityDAO.update(selectedActivity);
            table.refresh();
            clear();
            showAlert("Success", "Activity updated successfully", Alert.AlertType.INFORMATION);
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to update activity: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void deleteActivity() {
        if (selectedActivity == null) {
            showAlert("No Selection", "Please select an activity to delete", Alert.AlertType.WARNING);
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete Activity");
        confirmAlert.setContentText("Are you sure you want to delete this activity?");
        
        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                activityDAO.delete(selectedActivity.getIdActivity());
                loadAllActivities();  // Reload all activities
                clear();
                showAlert("Success", "Activity deleted successfully", Alert.AlertType.INFORMATION);
                
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error", "Failed to delete activity: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    public void clear() {
        employeeCombo.setValue(null);
        projectCombo.setValue(null);
        datePicker.setValue(null);
        descriptionField.clear();
        hoursField.clear();
        selectedActivity = null;
        table.getSelectionModel().clearSelection();
    }

    private boolean validate() {
        // Check employee
        if (employeeCombo.getValue() == null) {
            return false;
        }
        
        // Check project
        if (projectCombo.getValue() == null) {
            return false;
        }
        
        // Check date
        if (datePicker.getValue() == null) {
            return false;
        }
        
        // Check description
        if (descriptionField.getText() == null || descriptionField.getText().trim().isEmpty()) {
            return false;
        }
        
        // Check hours
        String hoursText = hoursField.getText();
        if (hoursText == null || hoursText.trim().isEmpty()) {
            return false;
        }
        
        try {
            int hours = Integer.parseInt(hoursText);
            if (hours <= 0) {
                return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }
        
        return true;
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
        // Navigation method
    @FXML
    private void goToProjects() {
        MainApp.loadProjectsView();
    }
}