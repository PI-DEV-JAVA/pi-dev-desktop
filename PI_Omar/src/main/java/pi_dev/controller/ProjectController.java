package pi_dev.controller;

import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import pi_dev.MainApp;
import pi_dev.dao.ProjectDAO;
import pi_dev.model.Project;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectController {

    @FXML private TextField nameField, budgetField, searchField;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<String> statusCombo;
    @FXML private DatePicker startDatePicker, endDatePicker;
    @FXML private VBox listContainer;
    @FXML private Button submitBtn;

    private final ProjectDAO projectDAO = new ProjectDAO();
    private final ObservableList<Project> masterList = FXCollections.observableArrayList();
    private Project selectedProject = null;

    @FXML
    private void initialize() {
        statusCombo.setItems(FXCollections.observableArrayList("PLANNED", "IN_PROGRESS", "DONE", "ON_HOLD"));
        
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String filter = newVal == null ? "" : newVal.toLowerCase();
            displayProjects(masterList.stream()
                .filter(p -> p.getName().toLowerCase().contains(filter) || p.getStatus().toLowerCase().contains(filter))
                .collect(Collectors.toList()));
        });

        loadProjects();
    }

    @FXML
    public void loadProjects() {
        masterList.setAll(projectDAO.getAll());
        displayProjects(masterList);
    }

    private void displayProjects(List<Project> projects) {
        listContainer.getChildren().clear();
        for (Project project : projects) {
            listContainer.getChildren().add(buildAdvancedCard(project));
        }
    }

    private VBox buildAdvancedCard(Project project) {
        // Main Container
        VBox card = new VBox(15);
        card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 15; " +
                     "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 10, 0, 0, 2);");

        // Header: Name and Status Badge
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label nameLbl = new Label(project.getName());
        nameLbl.setStyle("-fx-font-size: 18px; -fx-font-weight: 800; -fx-text-fill: #0D203B;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Status Badge Style
        Label statusBadge = new Label(project.getStatus());
        String baseBadgeStyle = "-fx-padding: 5 12; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;";
        statusBadge.setStyle(baseBadgeStyle + getStatusColorStyle(project.getStatus()));
        
        header.getChildren().addAll(nameLbl, spacer, statusBadge);

        // Body: Description
        Label descLbl = new Label(project.getDescription());
        descLbl.setWrapText(true);
        descLbl.setStyle("-fx-text-fill: #64748B; -fx-font-size: 14px;");

        // Footer: Metadata and Actions
        HBox footer = new HBox(20);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setStyle("-fx-padding: 10 0 0 0; -fx-border-color: #F1F5F9; -fx-border-width: 1 0 0 0;");

        VBox metaStart = new VBox(2, new Label("START DATE"), new Label(project.getStartDate().toString()));
        VBox metaEnd = new VBox(2, new Label("END DATE"), new Label(project.getEndDate().toString()));
        VBox metaBudget = new VBox(2, new Label("BUDGET"), new Label("$ " + String.format("%.2f", project.getBudget())));
        
        String metaTitleStyle = "-fx-text-fill: #94A3B8; -fx-font-size: 10px; -fx-font-weight: bold;";
        String metaValueStyle = "-fx-text-fill: #1E293B; -fx-font-size: 13px; -fx-font-weight: bold;";
        
        for (VBox v : new VBox[]{metaStart, metaEnd, metaBudget}) {
            v.getChildren().get(0).setStyle(metaTitleStyle);
            v.getChildren().get(1).setStyle(metaValueStyle);
        }

        Region footerSpacer = new Region();
        HBox.setHgrow(footerSpacer, Priority.ALWAYS);

        // Action Buttons
        Button editBtn = new Button("✎");
        editBtn.setStyle("-fx-background-color: #F1F5F9; -fx-text-fill: #0D203B; -fx-background-radius: 8; -fx-font-weight: bold;");
        editBtn.setOnAction(e -> populateForm(project));

        Button delBtn = new Button("🗑");
        delBtn.setStyle("-fx-background-color: #FFF1F2; -fx-text-fill: #EF4444; -fx-background-radius: 8; -fx-font-weight: bold;");
        delBtn.setOnAction(e -> handleDelete(project));

        footer.getChildren().addAll(metaStart, metaEnd, metaBudget, footerSpacer, editBtn, delBtn);

        card.getChildren().addAll(header, descLbl, footer);
        return card;
    }

    private String getStatusColorStyle(String status) {
        return switch (status) {
            case "DONE" -> "-fx-background-color: #DCFCE7; -fx-text-fill: #166534;";
            case "IN_PROGRESS" -> "-fx-background-color: #DBEAFE; -fx-text-fill: #1E40AF;";
            case "ON_HOLD" -> "-fx-background-color: #FEF3C7; -fx-text-fill: #92400E;";
            default -> "-fx-background-color: #F1F5F9; -fx-text-fill: #475569;";
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
        submitBtn.setStyle("-fx-background-color: #84A2AE; -fx-text-fill: #0D203B; -fx-font-weight: bold; -fx-background-radius: 10;");
        submitBtn.setOnAction(e -> updateProject());
    }

    @FXML
    private void addProject() {
        if (!validate()) return;
        projectDAO.add(new Project(0, nameField.getText(), descriptionField.getText(), statusCombo.getValue(), 
                                  startDatePicker.getValue(), endDatePicker.getValue(), Double.parseDouble(budgetField.getText())));
        loadProjects();
        clearFields();
    }

    private void updateProject() {
        if (selectedProject == null || !validate()) return;
        selectedProject.setName(nameField.getText());
        selectedProject.setDescription(descriptionField.getText());
        selectedProject.setStatus(statusCombo.getValue());
        selectedProject.setStartDate(startDatePicker.getValue());
        selectedProject.setEndDate(endDatePicker.getValue());
        selectedProject.setBudget(Double.parseDouble(budgetField.getText()));
        projectDAO.update(selectedProject);
        loadProjects();
        clearFields();
    }

    private void handleDelete(Project project) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete " + project.getName() + "?", ButtonType.YES, ButtonType.NO);
        if (alert.showAndWait().get() == ButtonType.YES) {
            projectDAO.delete(project.getId());
            loadProjects();
        }
    }

    @FXML
    private void clearFields() {
        nameField.clear(); descriptionField.clear(); budgetField.clear();
        statusCombo.setValue(null); startDatePicker.setValue(null); endDatePicker.setValue(null);
        selectedProject = null;
        submitBtn.setText("Create Project");
        submitBtn.setStyle("-fx-background-color: #0D203B; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10;");
        submitBtn.setOnAction(e -> addProject());
    }

    private boolean validate() {
        try {
            return !nameField.getText().isEmpty() && statusCombo.getValue() != null && Double.parseDouble(budgetField.getText()) >= 0;
        } catch (Exception e) { return false; }
    }

    @FXML private void goToActivities() { MainApp.loadActivitiesView(); }
}