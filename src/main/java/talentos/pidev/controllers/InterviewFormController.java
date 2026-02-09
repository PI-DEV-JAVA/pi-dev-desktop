package talentos.pidev.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import talentos.pidev.models.dao.InterviewDAO;
import talentos.pidev.models.schema.Interview;

public class InterviewFormController {

    @FXML
    private TextField titleField;
    // @FXML
    // private TextField recruiterField;
    @FXML
    private TextField candidateField;
    @FXML
    private ComboBox<String> statusBox;
    @FXML
    private TextField gradeField;

    private Interview interview;
    private InterviewDAO dao;
    private InterviewController parentController;

    @FXML
    public void initialize() {
        statusBox.getItems().addAll("PENDING", "IN_PROGRESS", "COMPLETED");
    }

    public void setInterview(Interview interview) {
        this.interview = interview;
        if (interview != null) {
            titleField.setText(interview.getTitle());
            // recruiterField.setText(String.valueOf(interview.getRecruiterId()));
            candidateField.setText(String.valueOf(interview.getCandidateId()));
            statusBox.setValue(interview.getStatus());
            gradeField.setText(interview.getGeneralGrade() != null ? interview.getGeneralGrade().toString() : "");
        }
    }

    public void setDao(InterviewDAO dao) {
        this.dao = dao;
    }

    public void setParentController(InterviewController parentController) {
        this.parentController = parentController;
    }

    @FXML
    private void onSave() {
        try {
            if (interview == null)
                interview = new Interview();

            interview.setTitle(titleField.getText());
            // interview.setRecruiterId(Long.parseLong(recruiterField.getText()));
            interview.setCandidateId(Long.parseLong(candidateField.getText()));
            interview.setStatus(statusBox.getValue());
            String gradeText = gradeField.getText();
            interview.setGeneralGrade(gradeText.isEmpty() ? null : Double.parseDouble(gradeText));

            if (interview.getId() == 0)
                dao.create(interview);
            else
                dao.update(interview);

            parentController.refresh();
            close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onCancel() {
        close();
    }

    private void close() {
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }
}
