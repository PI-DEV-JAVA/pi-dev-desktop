package talentospidev.controllers.Activities;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
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
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Candidate activity detail page.
 * View assigned activity + upload files / write notes to update the recruiter.
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
    private VBox filesContainer;
    @FXML
    private TextArea notesField;
    @FXML
    private VBox mainContent;

    private final ActivityDAO activityDAO = new ActivityDAO();
    private final ActivityFileDAO fileDAO = new ActivityFileDAO();
    private final ProjectDAO projectDAO = new ProjectDAO();
    private Activity currentActivity;
    private static final String UPLOAD_DIR = System.getProperty("user.home") + "/talentos_uploads/";
    @FXML
    private VBox sidebar;

    @FXML
    private void initialize() {
        talentospidev.utils.SidebarUtil.applySidebarIcons(sidebar);
        createUploadDirectory();
        int activityId = ViewContext.getSelectedActivityId();
        if (activityId > 0) {
            currentActivity = activityDAO.getById(activityId);
            if (currentActivity != null) {
                displayActivityDetails();
                loadFiles();
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
        SceneUtil.switchScene("dashboard.fxml");
    }

    @FXML
    private void handleJobOffers() {
        SceneUtil.switchScene("OffersCardView.fxml");
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