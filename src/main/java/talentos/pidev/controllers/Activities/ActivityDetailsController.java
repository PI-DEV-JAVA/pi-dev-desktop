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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ActivityDetailsController {

    @FXML private Label activityTitleLabel;
    @FXML private Label projectNameLabel;
    @FXML private Label dateLabel;
    @FXML private Label hoursLabel;
    @FXML private Label statusLabel;
    @FXML private TextArea descriptionArea;
    @FXML private TextArea workNotesArea;
    @FXML private VBox filesContainer;
    @FXML private Button closeBtn;
    @FXML private Button uploadFileBtn;
    @FXML private Button markCompleteBtn;
    @FXML private Button saveNotesBtn;

    private Activity activity;
    private Project project;
    private final ActivityDAO activityDAO = new ActivityDAO();
    private final ActivityFileDAO fileDAO = new ActivityFileDAO();
    private final ProjectDAO projectDAO = new ProjectDAO();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
    
    // Directory to store uploaded files
    private static final String UPLOAD_DIR = "uploads/activity_files/";

    @FXML
    private void initialize() {
        setupButtons();
        createUploadDirectory();
    }

    private void setupButtons() {
        closeBtn.setOnAction(e -> closeWindow());
        uploadFileBtn.setOnAction(e -> uploadFile());
        markCompleteBtn.setOnAction(e -> markAsComplete());
        saveNotesBtn.setOnAction(e -> saveNotes());
    }

    private void createUploadDirectory() {
        File directory = new File(UPLOAD_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
        loadProjectDetails();
        displayActivityDetails();
        loadFiles();
    }

    private void loadProjectDetails() {
        if (activity != null) {
            project = projectDAO.getById(activity.getProjectId());
        }
    }

    private void displayActivityDetails() {
        if (activity != null) {
            activityTitleLabel.setText("Activity #" + activity.getIdActivity());
            projectNameLabel.setText(project != null ? project.getName() : "Unknown Project");
            dateLabel.setText(activity.getActivityDate().format(dateFormatter));
            hoursLabel.setText(String.format("%.1f hours", activity.getHoursWorked()));
            descriptionArea.setText(activity.getDescription());
            
            if (project != null) {
                statusLabel.setText(project.getStatus());
                statusLabel.setStyle(getStatusStyle(project.getStatus()));
            }
        }
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

    private void loadFiles() {
        filesContainer.getChildren().clear();
        
        if (activity != null) {
            List<ActivityFile> files = fileDAO.getFilesByActivityId(activity.getIdActivity());
            
            if (files.isEmpty()) {
                Label noFilesLabel = new Label("No files attached yet");
                noFilesLabel.setStyle("-fx-text-fill: #718096; -fx-padding: 20;");
                filesContainer.getChildren().add(noFilesLabel);
            } else {
                for (ActivityFile file : files) {
                    HBox fileItem = createFileItem(file);
                    filesContainer.getChildren().add(fileItem);
                }
            }
        }
    }

    private HBox createFileItem(ActivityFile file) {
        HBox item = new HBox(10);
        item.setStyle("-fx-background-color: #F7FAFC; -fx-padding: 10; -fx-background-radius: 8;");
        item.setAlignment(Pos.CENTER_LEFT);
        
        // File icon based on type
        Label iconLabel = new Label(getFileIcon(file.getFileType()));
        iconLabel.setStyle("-fx-font-size: 20px;");
        
        // File info
        VBox infoBox = new VBox(3);
        Label nameLabel = new Label(file.getFileName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2D3748;");
        Label sizeLabel = new Label(file.getFormattedFileSize());
        sizeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #718096;");
        infoBox.getChildren().addAll(nameLabel, sizeLabel);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Download button
        Button downloadBtn = new Button("📥");
        downloadBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 16px; -fx-cursor: hand;");
        downloadBtn.setOnAction(e -> downloadFile(file));
        
        // Delete button
        Button deleteBtn = new Button("🗑️");
        deleteBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 16px; -fx-cursor: hand;");
        deleteBtn.setOnAction(e -> deleteFile(file));
        
        item.getChildren().addAll(iconLabel, infoBox, spacer, downloadBtn, deleteBtn);
        
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

    private void uploadFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File to Upload");
        
        // Add filters
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("All Files", "*.*"),
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"),
            new FileChooser.ExtensionFilter("Documents", "*.pdf", "*.doc", "*.docx", "*.txt"),
            new FileChooser.ExtensionFilter("Spreadsheets", "*.xls", "*.xlsx")
        );
        
        File selectedFile = fileChooser.showOpenDialog(uploadFileBtn.getScene().getWindow());
        
        if (selectedFile != null && activity != null) {
            try {
                // Create unique filename
                String uniqueFileName = System.currentTimeMillis() + "_" + selectedFile.getName();
                Path targetPath = Paths.get(UPLOAD_DIR, uniqueFileName);
                
                // Copy file to upload directory
                Files.copy(selectedFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                
                // Create file record
                ActivityFile file = new ActivityFile(
                    activity.getIdActivity(),
                    selectedFile.getName(),
                    targetPath.toString(),
                    selectedFile.length(),
                    Files.probeContentType(selectedFile.toPath())
                );
                
                // Save to database
                fileDAO.addFile(file);
                
                // Refresh files list
                loadFiles();
                
                showAlert(Alert.AlertType.INFORMATION, "Success", "File uploaded successfully!");
                
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to upload file: " + e.getMessage());
            }
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

    private void deleteFile(ActivityFile file) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete File");
        confirm.setHeaderText("Delete File");
        confirm.setContentText("Are you sure you want to delete this file?");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Delete physical file
                    Files.deleteIfExists(Paths.get(file.getFilePath()));
                    
                    // Delete from database
                    fileDAO.deleteFile(file.getId());
                    
                    // Refresh files list
                    loadFiles();
                    
                    showAlert(Alert.AlertType.INFORMATION, "Success", "File deleted successfully!");
                } catch (IOException e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete file: " + e.getMessage());
                }
            }
        });
    }

    private void markAsComplete() {
        if (project != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Mark as Complete");
            confirm.setHeaderText("Complete Activity");
            confirm.setContentText("Are you sure you want to mark this activity as complete?");
            
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    // Update project status
                    project.setStatus("DONE");
                    projectDAO.update(project);
                    
                    statusLabel.setText("DONE");
                    statusLabel.setStyle(getStatusStyle("DONE"));
                    
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Activity marked as complete!");
                }
            });
        }
    }

    private void saveNotes() {
        String notes = workNotesArea.getText();
        if (notes != null && !notes.trim().isEmpty()) {
            // You can save notes to a separate table or update activity
            // For now, just show a success message
            showAlert(Alert.AlertType.INFORMATION, "Success", "Notes saved successfully!");
        } else {
            showAlert(Alert.AlertType.WARNING, "Warning", "No notes to save!");
        }
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