package talentos.pidev.controllers.Activities;

import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import talentos.pidev.models.dao.ActivityDAO.ActivityDAO;
import talentos.pidev.models.dao.ProjectDAO.ProjectDAO;
import talentos.pidev.models.schema.Activity.Activity;
import talentos.pidev.models.schema.Project.Project;
import talentos.pidev.utils.DB;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class ActivityController {

    @FXML private ComboBox<Employee> employeeCombo;
    @FXML private ComboBox<Project> projectCombo;
    @FXML private DatePicker datePicker;
    @FXML private TextArea descriptionField;
    @FXML private TextField hoursField, searchField;
    @FXML private VBox listContainer;
    @FXML private Button submitBtn;

    private final ActivityDAO activityDAO = new ActivityDAO();
    private final ProjectDAO projectDAO = new ProjectDAO();
    private final ObservableList<Activity> masterList = FXCollections.observableArrayList();
    private Activity selectedActivity = null;
    private final Map<Integer, String> userEmails = new HashMap<>();

    // Small Employee wrapper class for ComboBox
    public static class Employee {
        private final int id;
        private final String email;

        public Employee(int id, String email) {
            this.id = id;
            this.email = email;
        }

        public int getId() { return id; }
        public String getEmail() { return email; }

        @Override
        public String toString() { return email; }
    }

    @FXML
    private void initialize() {
        loadUsers();
        loadProjects();
        loadAllActivities();

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String filter = newVal == null ? "" : newVal.toLowerCase();
            List<Activity> filtered = masterList.stream()
                .filter(a -> {
                    String email = userEmails.getOrDefault(a.getEmployeeId(), "").toLowerCase();
                    Project p = getProjectById(a.getProjectId());
                    String projName = (p != null) ? p.getName().toLowerCase() : "";
                    return email.contains(filter) || projName.contains(filter) || a.getDescription().toLowerCase().contains(filter);
                })
                .collect(Collectors.toList());
            displayActivities(filtered);
        });
    }

    private void loadUsers() {
        ObservableList<Employee> employees = FXCollections.observableArrayList();
        String sql = "SELECT id_user, email FROM users";
        try (Connection c = DB.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                int userId = rs.getInt("id_user");
                String email = rs.getString("email");
                employees.add(new Employee(userId, email));
                userEmails.put(userId, email);
            }
        } catch (Exception e) { e.printStackTrace(); }

        employeeCombo.setItems(employees);
        employeeCombo.setEditable(false); // prevent typing random text
    }

    private void loadProjects() {
        List<Project> projects = projectDAO.getAll();
        projectCombo.setItems(FXCollections.observableArrayList(projects));
        projectCombo.setEditable(false); // prevent typing random text
    }

    private Project getProjectById(int id) {
        return projectDAO.getAll().stream().filter(p -> p.getId() == id).findFirst().orElse(null);
    }

    @FXML
    public void loadAllActivities() {
        masterList.setAll(activityDAO.getAll());
        displayActivities(masterList);
    }

    private void displayActivities(List<Activity> activities) {
        listContainer.getChildren().clear();
        for (Activity activity : activities) {
            listContainer.getChildren().add(buildActivityCard(activity));
        }
    }

    private HBox buildActivityCard(Activity activity) {
        HBox card = new HBox(20);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 15; " +
                     "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 2);");

        VBox mainInfo = new VBox(4);
        mainInfo.setPrefWidth(200);
        Label userLbl = new Label(userEmails.getOrDefault(activity.getEmployeeId(), "Unknown User").toUpperCase());
        userLbl.setStyle("-fx-font-weight: 900; -fx-font-size: 11px; -fx-text-fill: #94A3B8; -fx-letter-spacing: 1px;");

        Project p = getProjectById(activity.getProjectId());
        Label projLbl = new Label(p != null ? p.getName() : "General Task");
        projLbl.setStyle("-fx-text-fill: #0D203B; -fx-font-weight: bold; -fx-font-size: 15px;");

        mainInfo.getChildren().addAll(userLbl, projLbl);

        VBox descContainer = new VBox(5);
        HBox.setHgrow(descContainer, Priority.ALWAYS);
        descContainer.setStyle("-fx-background-color: #F8FAFC; -fx-padding: 12; -fx-background-radius: 10; -fx-border-color: #E2E8F0; -fx-border-radius: 10;");

        Label descHeader = new Label("TASK DESCRIPTION");
        descHeader.setStyle("-fx-font-size: 9px; -fx-font-weight: 800; -fx-text-fill: #64748B;");

        Label descText = new Label(activity.getDescription());
        descText.setStyle("-fx-text-fill: #334155; -fx-font-size: 13px; -fx-line-spacing: 1.5;");
        descText.setWrapText(true);

        descContainer.getChildren().addAll(descHeader, descText);

        VBox stats = new VBox(2);
        stats.setAlignment(Pos.CENTER_RIGHT);
        stats.setMinWidth(80);

        Label hoursLbl = new Label(activity.getHours() + "h");
        hoursLbl.setStyle("-fx-font-weight: 900; -fx-font-size: 18px; -fx-text-fill: #0D203B;");

        Label dateLbl = new Label(activity.getDate().toString());
        dateLbl.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 11px; -fx-font-weight: bold;");

        stats.getChildren().addAll(hoursLbl, dateLbl);

        VBox actions = new VBox(8);
        actions.setAlignment(Pos.CENTER);

        Button editBtn = new Button("✎");
        editBtn.setTooltip(new Tooltip("Edit Record"));
        editBtn.setStyle("-fx-background-color: #F1F5F9; -fx-text-fill: #0D203B; -fx-background-radius: 8; -fx-cursor: hand;");
        editBtn.setOnAction(e -> populateForm(activity));

        Button delBtn = new Button("🗑");
        delBtn.setTooltip(new Tooltip("Delete Record"));
        delBtn.setStyle("-fx-background-color: #FFF1F2; -fx-text-fill: #EF4444; -fx-background-radius: 8; -fx-cursor: hand;");
        delBtn.setOnAction(e -> handleDelete(activity));

        actions.getChildren().addAll(editBtn, delBtn);

        card.getChildren().addAll(mainInfo, descContainer, stats, actions);
        return card;
    }

    private void populateForm(Activity activity) {
        selectedActivity = activity;
        employeeCombo.setValue(new Employee(activity.getEmployeeId(), userEmails.get(activity.getEmployeeId())));
        projectCombo.setValue(getProjectById(activity.getProjectId()));
        datePicker.setValue(activity.getDate());
        descriptionField.setText(activity.getDescription());
        hoursField.setText(String.valueOf(activity.getHours()));

        submitBtn.setText("Update Record");
        submitBtn.setStyle("-fx-background-color: #84A2AE; -fx-text-fill: #0D203B; -fx-font-weight: bold; -fx-background-radius: 10;");
        submitBtn.setOnAction(e -> updateActivity());
    }

    @FXML
    private void addActivity() {
        if (!validate()) return;
        Employee emp = employeeCombo.getValue();
        Project proj = projectCombo.getValue();

        activityDAO.add(new Activity(
                emp.getId(),
                proj.getId(),
                datePicker.getValue(),
                descriptionField.getText(),
                Integer.parseInt(hoursField.getText())
        ));

        loadAllActivities();
        clear();
    }

    private void updateActivity() {
        if (selectedActivity == null || !validate()) return;
        Employee emp = employeeCombo.getValue();
        Project proj = projectCombo.getValue();

        selectedActivity.setEmployeeId(emp.getId());
        selectedActivity.setProjectId(proj.getId());
        selectedActivity.setDate(datePicker.getValue());
        selectedActivity.setDescription(descriptionField.getText());
        selectedActivity.setHours(Integer.parseInt(hoursField.getText()));

        activityDAO.update(selectedActivity);
        loadAllActivities();
        clear();
    }

    private void handleDelete(Activity activity) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete this record?", ButtonType.YES, ButtonType.NO);
        if (alert.showAndWait().get() == ButtonType.YES) {
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
        submitBtn.setStyle("-fx-background-color: #0D203B; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10;");
        submitBtn.setOnAction(e -> addActivity());
    }

    private boolean validate() {
        try {
            return employeeCombo.getValue() != null &&
                   projectCombo.getValue() != null &&
                   datePicker.getValue() != null &&
                   Integer.parseInt(hoursField.getText()) > 0;
        } catch (Exception e) { return false; }
    }

    @FXML
    private void goToProjects() {
        talentos.pidev.controllers.MainLayoutController
                .getInstance()
                .navigate("projects.fxml", "Projects");
    }
}