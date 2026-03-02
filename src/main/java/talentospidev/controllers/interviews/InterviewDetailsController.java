package talentospidev.controllers.interviews;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import talentospidev.dao.interviewsDAO.InterviewMeetDAO;
import talentospidev.models.interviews.Interview;
import talentospidev.models.interviews.InterviewMeet;
import talentospidev.utils.generator;
import javafx.geometry.Insets;
// import talentos.pidev.models.dao.InterviewMeetDAO;
// import talentos.pidev.models.schema.Interview;
// import talentos.pidev.models.schema.InterviewMeet;
// import talentos.pidev.utils.generator;

import java.sql.SQLException;
import java.util.List;

// import org.jcp.xml.dsig.internal.dom.Utils;

public class InterviewDetailsController {

    private Interview interview;

    @FXML
    private Label titleLabel;
    @FXML
    private Label gradeLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Label createdAtLabel;

    @FXML
    private VBox sessionsContainer;

    // Add Session Form
    @FXML
    private VBox addSessionForm;
    @FXML
    private Button addSessionBtn;
    @FXML
    private DatePicker scheduledDatePicker;
    @FXML
    private TextField timeField;
    @FXML
    private TextField gradeField;
    @FXML
    private ChoiceBox<String> statusChoiceBox;

    @FXML
    private void initialize() {
        // Populate ChoiceBox manually (no FXCollections)
        statusChoiceBox.getItems().addAll("PENDING", "IN_PROGRESS", "COMPLETED");
        statusChoiceBox.setValue("PENDING");
    }

    public void setInterview(Interview interview) {
        this.interview = interview;
        updateUI();
        loadSessions();
    }

    private void updateUI() {
        if (interview != null) {
            titleLabel.setText(interview.getTitle());
            gradeLabel.setText(interview.getGeneralGrade() != null ? interview.getGeneralGrade() + " / 20" : "—");
            statusLabel.setText(interview.getStatus());
            createdAtLabel.setText(interview.getCreatedAt().toString());
        }
    }

    private void loadSessions() {
        sessionsContainer.getChildren().clear();
        try {
            InterviewMeetDAO meetDAO = new InterviewMeetDAO();
            List<InterviewMeet> meets = meetDAO.listByInterview(interview.getId());

            for (int i = 0; i < meets.size(); i++) {
                InterviewMeet meet = meets.get(i);
                HBox card = createMeetCard(meet, i);
                sessionsContainer.getChildren().add(card);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private HBox createMeetCard(InterviewMeet meet, int index) {
        HBox card = new HBox(20);
        card.setPadding(new Insets(16));
        card.setStyle(
                "-fx-background-color: " + (index % 2 == 0 ? "#f1f5f9" : "#f8fafc") + "; -fx-background-radius: 10;");

        VBox uuidBox = new VBox(2, new Label("UUID"), new Label(meet.getUuid()));
        uuidBox.getChildren().get(0).setStyle("-fx-font-size: 12; -fx-text-fill: #64748b;");
        uuidBox.getChildren().get(1).setStyle("-fx-font-weight: bold;");

        VBox scheduledBox = new VBox(2, new Label("Scheduled At"), new Label(meet.getScheduledAt().toString()));
        scheduledBox.getChildren().get(0).setStyle("-fx-font-size: 12; -fx-text-fill: #64748b;");
        scheduledBox.getChildren().get(1).setStyle("-fx-font-weight: bold;");

        VBox statusBox = new VBox(2, new Label("Status"), new Label(meet.getStatus()));
        statusBox.getChildren().get(0).setStyle("-fx-font-size: 12; -fx-text-fill: #64748b;");
        statusBox.getChildren().get(1)
                .setStyle("-fx-font-weight: bold; -fx-text-fill: " + statusColor(meet.getStatus()) + ";");

        VBox gradeBox = new VBox(2, new Label("Grade"),
                new Label(meet.getGrade() != null ? String.valueOf(meet.getGrade()) : "—"));
        gradeBox.getChildren().get(0).setStyle("-fx-font-size: 12; -fx-text-fill: #64748b;");
        gradeBox.getChildren().get(1).setStyle("-fx-font-weight: bold;");

        card.getChildren().addAll(uuidBox, scheduledBox, statusBox, gradeBox);
        return card;
    }

    private String statusColor(String status) {
        return switch (status) {
            case "COMPLETED" -> "#16a34a";
            case "PENDING" -> "#f59e0b";
            case "IN_PROGRESS" -> "#3b82f6";
            default -> "#000000";
        };
    }

    @FXML
    public void onBack() {
        MainLayoutController.getInstance().navigate("InterviewView.fxml", "Interviews");
    }

    // --- Add Session Form Logic ---
    @FXML
    private void onToggleAddSessionForm() {
        boolean visible = addSessionForm.isVisible();
        addSessionForm.setVisible(!visible);
        addSessionForm.setManaged(!visible);
    }

    @FXML
    private void onCancelAddSession() {
        addSessionForm.setVisible(false);
        addSessionForm.setManaged(false);
    }

    @FXML
private void onSaveAddSession() {
    try {
        // --- VALIDATION ---
        if (scheduledDatePicker.getValue() == null) {
            showAlert("Validation Error", "Please select a scheduled date.");
            return;
        }

        if (timeField.getText().isEmpty()) {
            showAlert("Validation Error", "Please enter a time in HH:mm format.");
            return;
        }

        int hour, minute;
        try {
            String[] parts = timeField.getText().split(":");
            if (parts.length != 2) throw new NumberFormatException();
            hour = Integer.parseInt(parts[0]);
            minute = Integer.parseInt(parts[1]);
            if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            showAlert("Validation Error", "Time must be in HH:mm format (00:00 - 23:59).");
            return;
        }

        // --- CHECK DATE + TIME IS NOT IN THE PAST ---
        java.time.LocalDateTime scheduled = scheduledDatePicker.getValue().atTime(hour, minute);
        if (scheduled.isBefore(java.time.LocalDateTime.now())) {
            showAlert("Validation Error", "Scheduled date and time cannot be in the past.");
            return;
        }

        Double grade = null;
        if (!gradeField.getText().isEmpty()) {
            try {
                grade = Double.parseDouble(gradeField.getText());
                if (grade < 0 || grade > 20) {
                    showAlert("Validation Error", "Grade must be between 0 and 20.");
                    return;
                }
            } catch (NumberFormatException ex) {
                showAlert("Validation Error", "Grade must be a number.");
                return;
            }
        }

        if (statusChoiceBox.getValue() == null || statusChoiceBox.getValue().isEmpty()) {
            showAlert("Validation Error", "Please select a status.");
            return;
        }

        // --- CREATE MEET ---
        InterviewMeet newMeet = new InterviewMeet();
        newMeet.setInterviewId(interview.getId());
        newMeet.setScheduledAt(scheduled);
        newMeet.setGrade(grade);
        newMeet.setStatus(statusChoiceBox.getValue());
        newMeet.setUuid(generator.generateUUID8()); // your UUID generator

        // Save to DB
        InterviewMeetDAO dao = new InterviewMeetDAO();
        dao.create(newMeet);

        // Add to UI
        int index = sessionsContainer.getChildren().size();
        HBox card = createMeetCard(newMeet, index);
        sessionsContainer.getChildren().add(card);

        // Reset form
        scheduledDatePicker.setValue(null);
        timeField.clear();
        gradeField.clear();
        statusChoiceBox.setValue("PENDING");
        addSessionForm.setVisible(false);
        addSessionForm.setManaged(false);

    } catch (Exception e) {
        e.printStackTrace();
        showAlert("Error", "An unexpected error occurred. Please try again.");
    }
}

// Helper method to show alerts
private void showAlert(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.WARNING);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
}

}
