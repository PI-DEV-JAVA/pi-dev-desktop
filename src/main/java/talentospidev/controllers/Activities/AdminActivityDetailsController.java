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
import talentospidev.services.AuthService;
import talentospidev.utils.DB;
import talentospidev.utils.SceneUtil;
import talentospidev.utils.ViewContext;
import javafx.stage.FileChooser;

import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Recruiter admin view of an activity — see candidate details + uploaded files.
 */
public class AdminActivityDetailsController {

    @FXML
    private Label employeeNameLabel;
    @FXML
    private Label employeeEmailLabel;
    @FXML
    private Label activityIdLabel;
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
    private VBox mainContent;

    private final ActivityDAO activityDAO = new ActivityDAO();
    private final ProjectDAO projectDAO = new ProjectDAO();
    private final ActivityFileDAO fileDAO = new ActivityFileDAO();
    private Activity currentActivity;
    @FXML
    private VBox sidebar;

    @FXML
    private void initialize() {
        talentospidev.utils.SidebarUtil.applySidebarIcons(sidebar);
        int activityId = ViewContext.getSelectedActivityId();
        if (activityId > 0) {
            currentActivity = activityDAO.getById(activityId);
            if (currentActivity != null) {
                loadEmployeeInfo();
                displayActivityDetails();
                displayFiles();
            }
        }
    }

    private void loadEmployeeInfo() {
        String sql = "SELECT u.email, p.first_name, p.last_name FROM users u LEFT JOIN profiles p ON p.user_id = u.id WHERE u.id = ?";
        try {
            Connection c = DB.getConnection();
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setInt(1, currentActivity.getEmployeeId());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String email = rs.getString("email");
                String fn = rs.getString("first_name");
                String ln = rs.getString("last_name");
                String name = (fn != null && ln != null) ? fn + " " + ln : email.split("@")[0];
                employeeNameLabel.setText(name);
                employeeEmailLabel.setText(email);
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void displayActivityDetails() {
        Project p = projectDAO.getById(currentActivity.getProjectId());
        activityIdLabel.setText("Activity #" + currentActivity.getIdActivity());
        projectNameLabel.setText(p != null ? p.getName() : "Unknown");
        dateLabel
                .setText("📅 " + currentActivity.getActivityDate().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
        hoursLabel.setText("⏱ " + String.format("%.1f hours", currentActivity.getHoursWorked()));
        descriptionLabel.setText(currentActivity.getDescription());

        if (p != null) {
            statusLabel.setText(p.getStatus());
            statusLabel.setStyle(getStatusStyle(p.getStatus()));
        }
    }

    private void displayFiles() {
        filesContainer.getChildren().clear();
        List<ActivityFile> files = fileDAO.getFilesByActivityId(currentActivity.getIdActivity());
        if (files.isEmpty()) {
            Label empty = new Label("No files attached");
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
        icon.setStyle("-fx-font-size: 16px;");
        VBox info = new VBox(1);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label name = new Label(file.getFileName());
        name.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: #111827;");
        Label size = new Label(file.getFormattedFileSize());
        size.setStyle("-fx-font-size: 10px; -fx-text-fill: #9ca3af;");
        info.getChildren().addAll(name, size);

        Button dlBtn = new Button("⬇");
        dlBtn.setStyle(
                "-fx-background-color: #eef2ff; -fx-text-fill: #6366f1; -fx-background-radius: 6; -fx-cursor: hand;");
        dlBtn.setOnAction(e -> downloadFile(file));

        row.getChildren().addAll(icon, info, dlBtn);
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

    private String getStatusStyle(String status) {
        return switch (status) {
            case "IN_PROGRESS" ->
                "-fx-background-color: #dbeafe; -fx-text-fill: #1e40af; -fx-padding: 4 12; -fx-background-radius: 10; -fx-font-size: 11px; -fx-font-weight: 700;";
            case "DONE" ->
                "-fx-background-color: #dcfce7; -fx-text-fill: #16a34a; -fx-padding: 4 12; -fx-background-radius: 10; -fx-font-size: 11px; -fx-font-weight: 700;";
            case "ON_HOLD" ->
                "-fx-background-color: #fef3c7; -fx-text-fill: #d97706; -fx-padding: 4 12; -fx-background-radius: 10; -fx-font-size: 11px; -fx-font-weight: 700;";
            default ->
                "-fx-background-color: #f3f4f6; -fx-text-fill: #6b7280; -fx-padding: 4 12; -fx-background-radius: 10; -fx-font-size: 11px; -fx-font-weight: 700;";
        };
    }

    private void downloadFile(ActivityFile file) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save File");
        chooser.setInitialFileName(file.getFileName());
        File dest = chooser.showSaveDialog(mainContent.getScene().getWindow());
        if (dest != null) {
            try {
                Files.copy(Path.of(file.getFilePath()), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                showAlert("Downloaded", "File saved to " + dest.getAbsolutePath(), Alert.AlertType.INFORMATION);
            } catch (IOException e) {
                showAlert("Error", "Download failed: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void downloadAllFiles() {
        List<ActivityFile> files = fileDAO.getFilesByActivityId(currentActivity.getIdActivity());
        if (files.isEmpty()) {
            showAlert("Info", "No files to download.", Alert.AlertType.INFORMATION);
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save ZIP");
        chooser.setInitialFileName("activity_" + currentActivity.getIdActivity() + "_files.zip");
        File dest = chooser.showSaveDialog(mainContent.getScene().getWindow());
        if (dest != null) {
            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(dest))) {
                for (ActivityFile f : files) {
                    File src = new File(f.getFilePath());
                    if (src.exists()) {
                        zos.putNextEntry(new ZipEntry(f.getFileName()));
                        Files.copy(src.toPath(), zos);
                        zos.closeEntry();
                    }
                }
                showAlert("Downloaded", "ZIP saved to " + dest.getAbsolutePath(), Alert.AlertType.INFORMATION);
            } catch (IOException e) {
                showAlert("Error", "ZIP failed: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void markAsReviewed() {
        if (currentActivity == null)
            return;
        String desc = currentActivity.getDescription();
        if (!desc.contains("[✅ REVIEWED]")) {
            currentActivity.setDescription("[✅ REVIEWED]\n" + desc);
            activityDAO.update(currentActivity);
            displayActivityDetails();
            showAlert("Marked", "Activity marked as reviewed!", Alert.AlertType.INFORMATION);
        }
    }

    private void showAlert(String t, String m, Alert.AlertType type) {
        Alert a = new Alert(type);
        a.setTitle(t);
        a.setHeaderText(null);
        a.setContentText(m);
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