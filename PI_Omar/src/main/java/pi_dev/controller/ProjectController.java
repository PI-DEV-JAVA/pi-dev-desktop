package pi_dev.controller;

import javafx.collections.*;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import pi_dev.MainApp;
import pi_dev.dao.ProjectDAO;
import pi_dev.model.Project;

import java.time.LocalDate;
import java.util.List;

public class ProjectController {

    @FXML private TextField nameField;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<String> statusCombo;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField budgetField;
    @FXML private TextField searchField;

    @FXML private TableView<Project> table;
    @FXML private TableColumn<Project, Integer> colId;
    @FXML private TableColumn<Project, String> colName;
    @FXML private TableColumn<Project, String> colDescription;
    @FXML private TableColumn<Project, String> colStatus;
    @FXML private TableColumn<Project, LocalDate> colStartDate;
    @FXML private TableColumn<Project, LocalDate> colEndDate;
    @FXML private TableColumn<Project, Double> colBudget;

    private final ProjectDAO projectDAO = new ProjectDAO();
    private final ObservableList<Project> masterList = FXCollections.observableArrayList();
    private FilteredList<Project> filteredList;

    private Project selectedProject;

    @FXML
    private void initialize() {
        // Load status options
        ObservableList<String> statusOptions = FXCollections.observableArrayList(
            "PLANNED", "IN_PROGRESS", "DONE", "ON_HOLD"
        );
        statusCombo.setItems(statusOptions);
        
        // Set up table columns
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStartDate.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        colEndDate.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        colBudget.setCellValueFactory(new PropertyValueFactory<>("budget"));
    
        // Initialize filtered list
        filteredList = new FilteredList<>(masterList, p -> true);
        table.setItems(filteredList);
    
        // Search functionality
        searchField.textProperty().addListener((obs, oldValue, newValue) ->
            filteredList.setPredicate(project -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                
                String lowerCaseFilter = newValue.toLowerCase();
                
                // Search by name
                if (project.getName().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                
                // Search by description
                if (project.getDescription().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                
                // Search by status
                if (project.getStatus().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                
                return false;
            })
        );
    
        // Table selection listener
        table.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> {
                selectedProject = newVal;
                if (newVal != null) {
                    populateForm(newVal);
                }
            }
        );
    
        // Load initial data
        loadProjects();
    }

    private void loadProjects() {
        List<Project> projects = projectDAO.getAll();
        masterList.setAll(projects);
    }

    private void populateForm(Project project) {
        if (project != null) {
            nameField.setText(project.getName());
            descriptionField.setText(project.getDescription());
            statusCombo.setValue(project.getStatus());
            startDatePicker.setValue(project.getStartDate());
            endDatePicker.setValue(project.getEndDate());
            budgetField.setText(String.valueOf(project.getBudget()));
        }
    }

    @FXML
    private void addProject() {
        if (!validate()) {
            showAlert("Validation Error", "Please fill all fields correctly", Alert.AlertType.WARNING);
            return;
        }

        try {
            Project project = new Project(
                0, // ID will be auto-generated
                nameField.getText(),
                descriptionField.getText(),
                statusCombo.getValue(),
                startDatePicker.getValue(),
                endDatePicker.getValue(),
                Double.parseDouble(budgetField.getText())
            );

            projectDAO.add(project);
            loadProjects();
            clearFields();
            showAlert("Success", "Project added successfully", Alert.AlertType.INFORMATION);
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to add project: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void updateProject() {
        if (selectedProject == null) {
            showAlert("No Selection", "Please select a project to update", Alert.AlertType.WARNING);
            return;
        }

        if (!validate()) {
            showAlert("Validation Error", "Please fill all fields correctly", Alert.AlertType.WARNING);
            return;
        }

        try {
            selectedProject.setName(nameField.getText());
            selectedProject.setDescription(descriptionField.getText());
            selectedProject.setStatus(statusCombo.getValue());
            selectedProject.setStartDate(startDatePicker.getValue());
            selectedProject.setEndDate(endDatePicker.getValue());
            selectedProject.setBudget(Double.parseDouble(budgetField.getText()));

            projectDAO.update(selectedProject);
            table.refresh();
            clearFields();
            showAlert("Success", "Project updated successfully", Alert.AlertType.INFORMATION);
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to update project: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void deleteProject() {
        if (selectedProject == null) {
            showAlert("No Selection", "Please select a project to delete", Alert.AlertType.WARNING);
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete Project");
        confirmAlert.setContentText("Are you sure you want to delete this project?\nThis will also delete all associated activities.");
        
        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                projectDAO.delete(selectedProject.getId());
                loadProjects();
                clearFields();
                showAlert("Success", "Project deleted successfully", Alert.AlertType.INFORMATION);
                
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error", "Failed to delete project: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    public void clearFields() {
        nameField.clear();
        descriptionField.clear();
        statusCombo.setValue(null);
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
        budgetField.clear();
        selectedProject = null;
        table.getSelectionModel().clearSelection();
    }

    private boolean validate() {
        // Check name
        if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
            return false;
        }
        
        // Check description
        if (descriptionField.getText() == null || descriptionField.getText().trim().isEmpty()) {
            return false;
        }
        
        // Check status
        if (statusCombo.getValue() == null || statusCombo.getValue().trim().isEmpty()) {
            return false;
        }
        
        // Check start date
        if (startDatePicker.getValue() == null) {
            return false;
        }
        
        // Check end date
        if (endDatePicker.getValue() == null) {
            return false;
        }
        
        // Check if end date is after start date
        if (endDatePicker.getValue().isBefore(startDatePicker.getValue())) {
            showAlert("Date Error", "End date must be after start date", Alert.AlertType.WARNING);
            return false;
        }
        
        // Check budget
        String budgetText = budgetField.getText();
        if (budgetText == null || budgetText.trim().isEmpty()) {
            return false;
        }
        
        try {
            double budget = Double.parseDouble(budgetText);
            if (budget < 0) {
                showAlert("Budget Error", "Budget must be a positive number", Alert.AlertType.WARNING);
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Budget Error", "Please enter a valid number for budget", Alert.AlertType.WARNING);
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
    private void goToActivities() {
        MainApp.loadActivitiesView();
    }
}