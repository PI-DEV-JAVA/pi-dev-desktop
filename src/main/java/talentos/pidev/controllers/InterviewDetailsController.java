package talentos.pidev.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import talentos.pidev.models.schema.Interview;

public class InterviewDetailsController {

    private Interview interview;

    @FXML
    private Label titleLabel;
    @FXML
    private Label recruiterLabel;
    @FXML
    private Label candidateLabel;
    @FXML
    private Label gradeLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Label createdAtLabel;

    public void setInterview(Interview interview) {
        this.interview = interview;
        updateUI();
    }

    private void updateUI() {
        if (interview != null) {
            titleLabel.setText(interview.getTitle());
            recruiterLabel.setText(String.valueOf(interview.getRecruiterId()));
            candidateLabel.setText(String.valueOf(interview.getCandidateId()));
            gradeLabel.setText(interview.getGeneralGrade() + " / 20");
            statusLabel.setText(interview.getStatus());
            createdAtLabel.setText(interview.getCreatedAt().toString());
        }
    }
}
