package talentos.pidev.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import talentos.pidev.models.dao.InterviewMeetDAO;
import talentos.pidev.models.schema.Interview;
import talentos.pidev.models.schema.InterviewMeet;

import java.sql.SQLException;
import java.util.List;

public class InterviewDetailsController {

    private Interview interview;

    @FXML private Label titleLabel;
    @FXML private Label recruiterLabel;
    @FXML private Label candidateLabel;
    @FXML private Label gradeLabel;
    @FXML private Label statusLabel;
    @FXML private Label createdAtLabel;

    @FXML private VBox sessionsContainer; 
    public void setInterview(Interview interview) {
        this.interview = interview;
        updateUI();
        loadSessions();
    }

    private void updateUI() {
        if (interview != null) {
            titleLabel.setText(interview.getTitle());
            recruiterLabel.setText(String.valueOf(interview.getRecruiterId()));
            candidateLabel.setText(String.valueOf(interview.getCandidateId()));
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
        card.setStyle("-fx-background-color: " + (index % 2 == 0 ? "#f1f5f9" : "#f8fafc") + "; -fx-background-radius: 10;");

        VBox uuidBox = new VBox(2);
        Label uuidLabel = new Label("UUID");
        uuidLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #64748b;");
        Label uuidValue = new Label(meet.getUuid());
        uuidValue.setStyle("-fx-font-weight: bold;");
        uuidBox.getChildren().addAll(uuidLabel, uuidValue);

        VBox scheduledBox = new VBox(2);
        Label schedLabel = new Label("Scheduled At");
        schedLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #64748b;");
        Label schedValue = new Label(meet.getScheduledAt().toString());
        schedValue.setStyle("-fx-font-weight: bold;");
        scheduledBox.getChildren().addAll(schedLabel, schedValue);

        VBox statusBox = new VBox(2);
        Label statusLabel = new Label("Status");
        statusLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #64748b;");
        Label statusValue = new Label(meet.getStatus());
        statusValue.setStyle("-fx-font-weight: bold; -fx-text-fill: " + statusColor(meet.getStatus()) + ";");
        statusBox.getChildren().addAll(statusLabel, statusValue);

        VBox gradeBox = new VBox(2);
        Label gradeLabel = new Label("Grade");
        gradeLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #64748b;");
        Label gradeValue = new Label(meet.getGrade() != null ? String.valueOf(meet.getGrade()) : "—");
        gradeValue.setStyle("-fx-font-weight: bold;");
        gradeBox.getChildren().addAll(gradeLabel, gradeValue);

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
}
