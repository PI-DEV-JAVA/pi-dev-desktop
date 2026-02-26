package talentos.pidev.controllers.Activities;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import talentos.pidev.models.dao.ActivityDAO.ActivityDAO;
import talentos.pidev.models.dao.ActivityDAO.ActivityFileDAO;
import talentos.pidev.models.dao.ProjectDAO.ProjectDAO;
import talentos.pidev.models.schema.Activity.Activity;
import talentos.pidev.models.schema.Activity.ActivityFile;
import talentos.pidev.models.schema.Project.Project;
import talentos.pidev.utils.DB;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class AdminActivityDetailsController {

    @FXML private Label employeeNameLabel;
    @FXML private Label employeeEmailLabel;
    @FXML private Label activityIdLabel;
    @FXML private Label projectNameLabel;
    @FXML private Label dateLabel;
    @FXML private Label hoursLabel;
    @FXML private Label statusLabel;
    @FXML private Label fileCountLabel;
    @FXML private TextArea descriptionArea;
    @FXML private TextArea employeeNotesArea;
    @FXML private VBox filesContainer;
    @FXML private Button closeBtn;
    @FXML private Button downloadAllBtn;
    @FXML private Button markReviewedBtn;

    private Activity activity;
    private Project project;
    private String employeeName;
    private String employeeEmail;
    private List<ActivityFile> files;
    
    private final ActivityDAO activityDAO = new ActivityDAO();
    private final ActivityFileDAO fileDAO = new ActivityFileDAO();
    private final ProjectDAO projectDAO = new ProjectDAO();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");

    @FXML
    private void initialize() {
        setupButtons();
    }

    private void setupButtons() {
        closeBtn.setOnAction(e -> closeWindow());
        downloadAllBtn.setOnAction(e -> downloadAllFiles());
        markReviewedBtn.setOnAction(e -> markAsReviewed());
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
        loadEmployeeInfo();
        loadProjectDetails();
        loadFiles();
        displayActivityDetails();
    }

    private void loadEmployeeInfo() {
        String sql = "SELECT email FROM users WHERE id_user = ?";
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, activity.getEmployeeId());
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                employeeEmail = rs.getString("email");
                // Extract name from email (before @)
                employeeName = employeeEmail.split("@")[0];
            } else {
                employeeName = "Unknown Employee";
                employeeEmail = "N/A";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            employeeName = "Unknown Employee";
            employeeEmail = "N/A";
        }
    }

    private void loadProjectDetails() {
        if (activity != null) {
            project = projectDAO.getById(activity.getProjectId());
        }
    }

    private void loadFiles() {
        if (activity != null) {
            files = fileDAO.getFilesByActivityId(activity.getIdActivity());
        }
    }

    private void displayActivityDetails() {
        // Employee info
        employeeNameLabel.setText(employeeName);
        employeeEmailLabel.setText(employeeEmail);
        activityIdLabel.setText("Activity #" + activity.getIdActivity());
        
        // Activity details
        projectNameLabel.setText(project != null ? project.getName() : "Unknown Project");
        dateLabel.setText(activity.getActivityDate().format(dateFormatter));
        hoursLabel.setText(String.format("%.1f hours", activity.getHoursWorked()));
        descriptionArea.setText(activity.getDescription());
        
        // Status
        if (project != null) {
            statusLabel.setText(project.getStatus());
            statusLabel.setStyle(getStatusStyle(project.getStatus()));
        }
        
        // Notes (if you have a notes table, otherwise hide)
        employeeNotesArea.setText("No additional notes");
        
        // Display files
        displayFiles();
    }

    private void displayFiles() {
        filesContainer.getChildren().clear();
        
        if (files == null || files.isEmpty()) {
            Label noFilesLabel = new Label("No files attached to this activity");
            noFilesLabel.setStyle("-fx-text-fill: #718096; -fx-padding: 20;");
            noFilesLabel.setAlignment(Pos.CENTER);
            filesContainer.getChildren().add(noFilesLabel);
            fileCountLabel.setText("(0 files)");
        } else {
            fileCountLabel.setText("(" + files.size() + " file" + (files.size() > 1 ? "s" : "") + ")");
            
            for (ActivityFile file : files) {
                HBox fileItem = createFileItem(file);
                filesContainer.getChildren().add(fileItem);
            }
        }
    }

    private HBox createFileItem(ActivityFile file) {
        HBox item = new HBox(10);
        item.setStyle("-fx-background-color: #F8FAFC; -fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #E2E8F0; -fx-border-radius: 8;");
        item.setAlignment(Pos.CENTER_LEFT);
        
        // File icon based on type
        Label iconLabel = new Label(getFileIcon(file.getFileType()));
        iconLabel.setStyle("-fx-font-size: 24px;");
        
        // File info
        VBox infoBox = new VBox(3);
        Label nameLabel = new Label(file.getFileName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2D3748;");
        
        Label metaLabel = new Label(file.getFormattedFileSize() + " • Uploaded " + 
            file.getUploadedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")));
        metaLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #718096;");
        
        infoBox.getChildren().addAll(nameLabel, metaLabel);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Download button
        Button downloadBtn = new Button("📥 Download");
        downloadBtn.setStyle("-fx-background-color: #0D203B; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5 12; -fx-background-radius: 5; -fx-cursor: hand;");
        downloadBtn.setOnAction(e -> downloadFile(file));
        
        item.getChildren().addAll(iconLabel, infoBox, spacer, downloadBtn);
        
        return item;
    }

    private String getFileIcon(String fileType) {
        if (fileType == null) return "📄";
        
        if (fileType.contains("image")) return "🖼️";
        if (fileType.contains("pdf")) return "📕";
        if (fileType.contains("word") || fileType.contains("document")) return "📘";
        if (fileType.contains("excel") || fileType.contains("sheet")) return "📗";
        if (fileType.contains("zip") || fileType.contains("archive")) return "📦";
        if (fileType.contains("text")) return "📃";
        
        return "📄";
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

    private void downloadFile(ActivityFile file) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save File");
        fileChooser.setInitialFileName(file.getFileName());
        
        File saveLocation = fileChooser.showSaveDialog(closeBtn.getScene().getWindow());
        
        if (saveLocation != null) {
            try {
                Path sourcePath = Paths.get(file.getFilePath());
                Files.copy(sourcePath, saveLocation.toPath(), StandardCopyOption.REPLACE_EXISTING);
                showAlert(Alert.AlertType.INFORMATION, "Success", "File downloaded successfully!");
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to download file: " + e.getMessage());
            }
        }
    }

    private void downloadAllFiles() {
        if (files == null || files.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Files", "No files to download.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save ZIP Archive");
        fileChooser.setInitialFileName("activity_" + activity.getIdActivity() + "_files.zip");
        
        File saveLocation = fileChooser.showSaveDialog(closeBtn.getScene().getWindow());
        
        if (saveLocation != null) {
            try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(saveLocation.toPath()))) {
                for (ActivityFile file : files) {
                    Path filePath = Paths.get(file.getFilePath());
                    if (Files.exists(filePath)) {
                        zos.putNextEntry(new ZipEntry(file.getFileName()));
                        Files.copy(filePath, zos);
                        zos.closeEntry();
                    }
                }
                showAlert(Alert.AlertType.INFORMATION, "Success", "All files downloaded successfully!");
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to create ZIP file: " + e.getMessage());
            }
        }
    }

    private void markAsReviewed() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Mark as Reviewed");
        confirm.setHeaderText("Mark Activity as Reviewed");
        confirm.setContentText("Have you reviewed this activity and its files?");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // You could add a "reviewed" flag to the activity table
                // For now, just show success message
                showAlert(Alert.AlertType.INFORMATION, "Success", "Activity marked as reviewed!");
                markReviewedBtn.setDisable(true);
                markReviewedBtn.setText("✓ Reviewed");
            }
        });
    }

    private void closeWindow() {
        Stage stage = (Stage) closeBtn.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}